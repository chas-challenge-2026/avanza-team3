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
import se.comerit.avanza.targetallocation.service.TargetAllocationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private static final BigDecimal DRIFT_THRESHOLD = new BigDecimal("0.05");
    private static final List<String> ACCOUNT_TYPES = List.of("ISK", "KF", "Depa", "Pension");

    private final AccountService accountService;
    private final HoldingService holdingService;
    private final TargetAllocationService targetAllocationService;
    private final AlertService alertService;
    private final MarketDataService marketDataService;

    public DashboardService(AccountService accountService,
                            HoldingService holdingService,
                            TargetAllocationService targetAllocationService,
                            AlertService alertService,
                            MarketDataService marketDataService) {
        this.accountService = accountService;
        this.holdingService = holdingService;
        this.targetAllocationService = targetAllocationService;
        this.alertService = alertService;
        this.marketDataService = marketDataService;
    }

    @Transactional(readOnly = true)
    public DashboardView getDashboardForUser(Integer userId) {
        return getDashboardForUser(userId, 0, 20);
    }

    @Transactional(readOnly = true)
    public DashboardView getDashboardForUser(Integer userId, int page, int size) {
        List<Map<String, Object>> accounts = accountService.getAccountMapsByUserId(userId);
        List<Map<String, Object>> holdingsForTotals = holdingService.getHoldingsByUserId(userId);
        Page<Map<String, Object>> holdingsPage = holdingService.getHoldingsByUserId(userId, page, size);
        List<Map<String, Object>> targets = targetAllocationService.getTargetMapsByUserId(userId);

        Map<Integer, String> accountTypeById = createAccountTypeById(accounts);
        Map<Integer, BigDecimal> accountTotals = new HashMap<>();
        Map<String, BigDecimal> accountTypeTotals = createAccountTypeTotals();
        BigDecimal usdToSek = marketDataService.getFxRate("USD", "SEK");

        BigDecimal totalPortfolioValue = BigDecimal.ZERO;

        for (Map<String, Object> holding : holdingsForTotals) {
            HoldingSummaryResponse enriched = enrichHolding(holding, usdToSek);
            BigDecimal valueSek = enriched.valueSek();

            Integer accountId = (Integer) holding.get("account_id");
            String accountType = accountTypeById.getOrDefault(accountId, "Depa");

            accountTotals.merge(accountId, valueSek, BigDecimal::add);
            accountTypeTotals.merge(accountType, valueSek, BigDecimal::add);
            totalPortfolioValue = totalPortfolioValue.add(valueSek);
        }

        AllocationResult allocationResult = createAllocationRows(
                targets,
                accountTypeTotals,
                totalPortfolioValue
        );

        return new DashboardView(
                createAccountSummary(accounts, accountTotals),
                createHoldingPage(holdingsPage, usdToSek),
                allocationResult.rows(),
                roundToDouble(totalPortfolioValue),
                getRecentAlerts(userId),
                allocationResult.anyDrift(),
                usdToSek.doubleValue()
        );
    }

    private HoldingPageResponse createHoldingPage(Page<Map<String, Object>> holdingsPage, BigDecimal usdToSek) {
        List<HoldingSummaryResponse> content = holdingsPage.getContent().stream()
                .map(holding -> enrichHolding(holding, usdToSek))
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

    private Map<Integer, String> createAccountTypeById(List<Map<String, Object>> accounts) {
        Map<Integer, String> accountTypeById = new HashMap<>();
        for (Map<String, Object> account : accounts) {
            accountTypeById.put((Integer) account.get("id"), (String) account.get("account_type"));
        }
        return accountTypeById;
    }

    private Map<String, BigDecimal> createAccountTypeTotals() {
        Map<String, BigDecimal> accountTypeTotals = new HashMap<>();
        for (String accountType : ACCOUNT_TYPES) {
            accountTypeTotals.put(accountType, BigDecimal.ZERO);
        }
        return accountTypeTotals;
    }

    private HoldingSummaryResponse enrichHolding(Map<String, Object> holding, BigDecimal usdToSek) {
        String ticker = (String) holding.get("ticker");
        String currency = (String) holding.get("currency");
        BigDecimal quantity = toBigDecimal(holding.get("quantity"));
        BigDecimal avgBuy = toBigDecimal(holding.get("avg_buy_price"));
        BigDecimal currentPrice = marketDataService.getPrice(ticker);

        BigDecimal valueSek = quantity.multiply(currentPrice).multiply(currencyMultiplier(currency, usdToSek));
        BigDecimal costBasis = quantity.multiply(avgBuy).multiply(currencyMultiplier(currency, usdToSek));
        BigDecimal unrealizedReturn = valueSek.subtract(costBasis);
        BigDecimal unrealizedReturnPct = costBasis.compareTo(BigDecimal.ZERO) > 0
                ? unrealizedReturn.divide(costBasis, 6, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;
        BigDecimal sharpe = unrealizedReturnPct.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)
                .subtract(new BigDecimal("0.02"))
                .divide(new BigDecimal("0.15"), 6, RoundingMode.HALF_UP);

        return new HoldingSummaryResponse(
                (Integer) holding.get("id"),
                (Integer) holding.get("account_id"),
                ticker,
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
                "USD".equals(currency) ? "USD->SEK" : "SEK"
        );
    }

    private BigDecimal currencyMultiplier(String currency, BigDecimal usdToSek) {
        return "USD".equals(currency) ? usdToSek : BigDecimal.ONE;
    }

    private AllocationResult createAllocationRows(List<Map<String, Object>> targets,
                                                  Map<String, BigDecimal> accountTypeTotals,
                                                  BigDecimal totalPortfolioValue) {
        Map<String, BigDecimal> targetMap = new HashMap<>();
        for (Map<String, Object> target : targets) {
            targetMap.put((String) target.get("account_type"), toBigDecimal(target.get("target_pct")));
        }

        boolean anyDrift = false;
        List<AllocationRowResponse> allocationRows = new ArrayList<>();

        for (String accountType : ACCOUNT_TYPES) {
            BigDecimal actual = totalPortfolioValue.compareTo(BigDecimal.ZERO) > 0
                    ? accountTypeTotals.getOrDefault(accountType, BigDecimal.ZERO)
                    .divide(totalPortfolioValue, 6, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;
            BigDecimal target = targetMap.getOrDefault(accountType, BigDecimal.ZERO);
            BigDecimal drift = actual.subtract(target).abs().divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
            boolean overThreshold = drift.compareTo(DRIFT_THRESHOLD) > 0;

            allocationRows.add(new AllocationRowResponse(
                    accountType,
                    round(actual),
                    target,
                    round(drift.multiply(new BigDecimal("100"))),
                    overThreshold
            ));

            if (overThreshold) {
                anyDrift = true;
            }
        }

        return new AllocationResult(allocationRows, anyDrift);
    }

    private List<AccountSummaryResponse> createAccountSummary(List<Map<String, Object>> accounts,
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

    private record AllocationResult(List<AllocationRowResponse> rows, boolean anyDrift) {
    }
}
