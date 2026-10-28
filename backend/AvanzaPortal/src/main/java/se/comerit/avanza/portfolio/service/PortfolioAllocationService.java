package se.comerit.avanza.portfolio.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.comerit.avanza.portfolio.dto.PortfolioAllocationResult;
import se.comerit.avanza.portfolio.dto.PortfolioAllocationRow;
import se.comerit.avanza.portfolio.dto.ValuedHolding;
import se.comerit.avanza.targetallocation.model.TargetAllocation;
import se.comerit.avanza.targetallocation.service.TargetAllocationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PortfolioAllocationService {

    private static final BigDecimal DRIFT_THRESHOLD_PERCENTAGE_POINTS = new BigDecimal("5.00");
    private static final List<String> DEFAULT_ACCOUNT_TYPES = List.of("ISK", "KF", "Depa", "Pension");

    private final PortfolioValuationService portfolioValuationService;
    private final TargetAllocationService targetAllocationService;

    public PortfolioAllocationService(
            PortfolioValuationService portfolioValuationService,
            TargetAllocationService targetAllocationService) {
        this.portfolioValuationService = portfolioValuationService;
        this.targetAllocationService = targetAllocationService;
    }

    @Transactional(readOnly = true)
    public PortfolioAllocationResult getAccountTypeAllocation(Integer userId) {
        List<ValuedHolding> holdings = portfolioValuationService.getValuedHoldingsByUserId(userId);
        return getAccountTypeAllocation(userId, holdings);
    }

    @Transactional(readOnly = true)
    public PortfolioAllocationResult getAccountTypeAllocation(
            Integer userId,
            List<ValuedHolding> holdings) {
        return calculateAccountTypeAllocation(
                holdings,
                targetAllocationService.getTargetAllocationsByUserId(userId)
        );
    }

    public PortfolioAllocationResult calculateAccountTypeAllocation(
            List<ValuedHolding> holdings,
            List<TargetAllocation> targets) {

        Map<String, BigDecimal> accountTypeTotals = new HashMap<>();
        BigDecimal totalPortfolioValueSek = BigDecimal.ZERO;

        Set<String> accountTypes = new LinkedHashSet<>(DEFAULT_ACCOUNT_TYPES);

        for (ValuedHolding holding : holdings) {
            String accountType = normalizeAccountType(holding.accountType());
            accountTypes.add(accountType);
            accountTypeTotals.merge(accountType, holding.marketValueSek(), BigDecimal::add);
            totalPortfolioValueSek = totalPortfolioValueSek.add(holding.marketValueSek());
        }

        Map<String, BigDecimal> targetMap = new HashMap<>();
        for (TargetAllocation target : targets) {
            String accountType = normalizeAccountType(target.getAccountType());
            accountTypes.add(accountType);
            targetMap.put(accountType, orZero(target.getTargetPct()));
        }

        List<PortfolioAllocationRow> rows = new ArrayList<>();
        boolean anyDrift = false;

        for (String accountType : accountTypes) {
            BigDecimal actualPercentage = totalPortfolioValueSek.compareTo(BigDecimal.ZERO) > 0
                    ? accountTypeTotals.getOrDefault(accountType, BigDecimal.ZERO)
                    .divide(totalPortfolioValueSek, 8, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;

            BigDecimal targetPercentage = targetMap.getOrDefault(accountType, BigDecimal.ZERO);
            BigDecimal driftPercentagePoints = actualPercentage.subtract(targetPercentage).abs();
            boolean overThreshold = driftPercentagePoints.compareTo(DRIFT_THRESHOLD_PERCENTAGE_POINTS) > 0;

            rows.add(new PortfolioAllocationRow(
                    accountType,
                    round(actualPercentage),
                    round(targetPercentage),
                    round(driftPercentagePoints),
                    overThreshold
            ));

            anyDrift = anyDrift || overThreshold;
        }

        return new PortfolioAllocationResult(
                List.copyOf(rows),
                round(totalPortfolioValueSek),
                anyDrift
        );
    }

    public List<se.comerit.avanza.portfolio.dto.InstrumentAllocationRow> calculateInstrumentTypeAllocation(
            List<ValuedHolding> holdings) {
        Map<String, BigDecimal> totals = new java.util.LinkedHashMap<>();
        totals.put("Aktie", BigDecimal.ZERO);
        totals.put("Fond", BigDecimal.ZERO);
        totals.put("Övrigt", BigDecimal.ZERO);
        BigDecimal portfolioTotal = BigDecimal.ZERO;
        for (ValuedHolding holding : holdings) {
            String category = holding.instrumentType() == null
                    ? "Övrigt" : holding.instrumentType().getDisplayName();
            BigDecimal marketValue = orZero(holding.marketValueSek());
            totals.merge(category, marketValue, BigDecimal::add);
            portfolioTotal = portfolioTotal.add(marketValue);
        }
        List<se.comerit.avanza.portfolio.dto.InstrumentAllocationRow> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : totals.entrySet()) {
            BigDecimal percent = portfolioTotal.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().multiply(new BigDecimal("100"))
                    .divide(portfolioTotal, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2);
            result.add(new se.comerit.avanza.portfolio.dto.InstrumentAllocationRow(
                    entry.getKey(), round(entry.getValue()), percent));
        }
        return List.copyOf(result);
    }

    public int getDriftThresholdPercent() {
        return DRIFT_THRESHOLD_PERCENTAGE_POINTS.intValue();
    }

    private String normalizeAccountType(String accountType) {
        if (accountType == null || accountType.isBlank()) {
            return "Depa";
        }
        return accountType;
    }

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
