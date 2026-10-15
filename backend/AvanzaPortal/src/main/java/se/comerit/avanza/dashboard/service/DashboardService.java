package se.comerit.avanza.dashboard.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.comerit.avanza.account.service.AccountService;
import se.comerit.avanza.alert.dto.AlertResponse;
import se.comerit.avanza.alert.service.AlertService;
import se.comerit.avanza.dashboard.dto.AccountSummaryResponse;
import se.comerit.avanza.dashboard.dto.AllocationRowResponse;
import se.comerit.avanza.dashboard.dto.DashboardView;
import se.comerit.avanza.dashboard.dto.HoldingPageResponse;
import se.comerit.avanza.dashboard.dto.HoldingSummaryResponse;
import se.comerit.avanza.dashboard.dto.RecentAlertResponse;
import se.comerit.avanza.holding.service.HoldingService;
import se.comerit.avanza.market.service.MarketDataService;
import se.comerit.avanza.portfolio.dto.PortfolioAllocationResult;
import se.comerit.avanza.portfolio.dto.PortfolioAllocationRow;
import se.comerit.avanza.portfolio.dto.ValuedHolding;
import se.comerit.avanza.portfolio.service.PortfolioAllocationService;
import se.comerit.avanza.portfolio.service.PortfolioValuationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private static final String BASE_CURRENCY = PortfolioValuationService.BASE_CURRENCY;

    private final AccountService accountService;
    private final HoldingService holdingService;
    private final AlertService alertService;
    private final MarketDataService marketDataService;
    private final PortfolioAllocationService portfolioAllocationService;
    private final PortfolioValuationService portfolioValuationService;

    public DashboardService(
            AccountService accountService,
            HoldingService holdingService,
            AlertService alertService,
            MarketDataService marketDataService,
            PortfolioAllocationService portfolioAllocationService,
            PortfolioValuationService portfolioValuationService) {
        this.accountService = accountService;
        this.holdingService = holdingService;
        this.alertService = alertService;
        this.marketDataService = marketDataService;
        this.portfolioAllocationService = portfolioAllocationService;
        this.portfolioValuationService = portfolioValuationService;
    }

    @Transactional(readOnly = true)
    public DashboardView getDashboardForUser(Integer userId) {
        return getDashboardForUser(userId, 0, 20);
    }

    @Transactional(readOnly = true)
    public DashboardView getDashboardForUser(Integer userId, int page, int size) {
        List<Map<String, Object>> accounts = accountService.getAccountMapsByUserId(userId);
        Page<Map<String, Object>> holdingsPage = holdingService.getHoldingsByUserId(userId, page, size);
        List<ValuedHolding> valuedHoldings =
                portfolioValuationService.getValuedHoldingsByUserId(userId);

        Map<Integer, BigDecimal> accountTotals = new HashMap<>();
        for (ValuedHolding holding : valuedHoldings) {
            accountTotals.merge(
                    holding.accountId(),
                    holding.marketValueSek(),
                    BigDecimal::add
            );
        }

        PortfolioAllocationResult allocationResult =
                portfolioAllocationService.getAccountTypeAllocation(userId, valuedHoldings);

        BigDecimal totalPortfolioValue = allocationResult.totalPortfolioValueSek();
        BigDecimal usdToSek = marketDataService.getEffectiveFxRate("USD", BASE_CURRENCY);

        return new DashboardView(
                createAccountSummary(accounts, accountTotals),
                createHoldingPage(holdingsPage),
                toDashboardAllocationRows(allocationResult.rows()),
                roundToDouble(totalPortfolioValue),
                getRecentAlerts(userId),
                allocationResult.anyDrift(),
                usdToSek.doubleValue()
        );
    }

    private HoldingPageResponse createHoldingPage(Page<Map<String, Object>> holdingsPage) {
        List<HoldingSummaryResponse> content = holdingsPage.getContent().stream()
                .map(this::enrichHolding)
                .toList();

        return new HoldingPageResponse(
                content,
                holdingsPage.getNumber(),
                holdingsPage.getSize(),
                holdingsPage.getTotalElements(),
                holdingsPage.getTotalPages(),
                holdingsPage.isFirst(),
                holdingsPage.isLast()
        );
    }

    private HoldingSummaryResponse enrichHolding(Map<String, Object> holding) {
        String currency = (String) holding.get("currency");
        BigDecimal quantity = toBigDecimal(holding.get("quantity"));
        BigDecimal avgBuy = toBigDecimal(holding.get("avg_buy_price"));
        BigDecimal currentPrice = toBigDecimal(holding.get("currentPrice"));
        BigDecimal valueSek = toBigDecimal(holding.get("marketValue"));
        BigDecimal unrealizedReturn = toBigDecimal(holding.get("pnl"));
        BigDecimal unrealizedReturnPct = toBigDecimal(holding.get("pnlPct"));

        // Legacy dashboard field kept for API compatibility in this integration.
        // Portfolio-level Sharpe belongs to PortfolioRiskService/C and will be
        // replaced by the dashboard/risk owner together with real historical data.
        BigDecimal sharpe = unrealizedReturnPct.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)
                .subtract(new BigDecimal("0.02"))
                .divide(new BigDecimal("0.15"), 6, RoundingMode.HALF_UP);

        return new HoldingSummaryResponse(
                (Integer) holding.get("id"),
                (Integer) holding.get("account_id"),
                (String) holding.get("ticker"),
                (String) holding.get("instrument_name"),
                quantity,
                avgBuy,
                currency,
                (String) holding.get("account_type"),
                (String) holding.get("account_name"),
                round(currentPrice),
                round(valueSek),
                round(unrealizedReturn),
                round(unrealizedReturnPct),
                round(sharpe),
                BASE_CURRENCY.equals(currency) ? BASE_CURRENCY : currency + "->" + BASE_CURRENCY
        );
    }

    private List<AllocationRowResponse> toDashboardAllocationRows(
            List<PortfolioAllocationRow> rows) {
        return rows.stream()
                .map(row -> new AllocationRowResponse(
                        row.accountType(),
                        row.actualPercentage(),
                        row.targetPercentage(),
                        row.driftPercentagePoints(),
                        row.overThreshold()
                ))
                .toList();
    }

    private List<AccountSummaryResponse> createAccountSummary(
            List<Map<String, Object>> accounts,
            Map<Integer, BigDecimal> accountTotals) {

        List<AccountSummaryResponse> accountSummary = new ArrayList<>();

        for (Map<String, Object> account : accounts) {
            Integer accountId = (Integer) account.get("id");
            accountSummary.add(new AccountSummaryResponse(
                    accountId,
                    (Integer) account.get("user_id"),
                    (String) account.get("account_type"),
                    (String) account.get("account_name"),
                    (String) account.get("currency"),
                    round(accountTotals.getOrDefault(accountId, BigDecimal.ZERO))
            ));
        }

        return accountSummary;
    }

    private List<RecentAlertResponse> getRecentAlerts(Integer userId) {
        return alertService.getAlertsByUserId(userId).stream()
                .filter(alert -> !alert.dismissed())
                .map(this::toRecentAlert)
                .toList();
    }

    private RecentAlertResponse toRecentAlert(AlertResponse alert) {
        return new RecentAlertResponse(
                alert.id(),
                alert.alertType(),
                alert.message(),
                alert.createdAt()
        );
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }

        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }

        return new BigDecimal(value.toString());
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private double roundToDouble(BigDecimal value) {
        return round(value).doubleValue();
    }
}
