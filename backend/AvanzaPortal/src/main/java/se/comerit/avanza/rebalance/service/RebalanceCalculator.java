package se.comerit.avanza.rebalance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.springframework.stereotype.Service;
import se.comerit.avanza.accountallocation.model.AssetCategory;
import se.comerit.avanza.accountallocation.model.AllocationRules;
import se.comerit.avanza.exception.BusinessRuleException;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.portfolio.dto.ValuedHolding;
import se.comerit.avanza.rebalance.dto.RebalanceAllocationRow;
import se.comerit.avanza.rebalance.dto.RebalanceTradeResponse;
import se.comerit.avanza.rebalance.dto.RebalanceProposedHoldingResponse;

@Service
public class RebalanceCalculator {
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public record Quote(Instrument instrument, BigDecimal priceNative, BigDecimal fxToSek) {
        public BigDecimal unitPriceSek() { return priceNative.multiply(fxToSek); }
    }

    public record Calculation(BigDecimal totalValueSek, BigDecimal cashBeforeSek,
                              BigDecimal cashAfterSek, boolean executable,
                              List<RebalanceAllocationRow> before,
                              List<RebalanceAllocationRow> after,
                              List<RebalanceTradeResponse> trades,
                              List<RebalanceProposedHoldingResponse> proposedHoldings,
                              List<String> warnings) {}

    private static final class Position {
        final Integer instrumentId;
        final String ticker;
        final String instrumentName;
        final AssetCategory category;
        final BigDecimal unitPriceSek;
        BigDecimal quantity;

        Position(Integer id, String ticker, String instrumentName, AssetCategory category,
                 BigDecimal priceSek, BigDecimal quantity) {
            this.instrumentId = id;
            this.ticker = ticker;
            this.instrumentName = instrumentName;
            this.category = category;
            this.unitPriceSek = priceSek;
            this.quantity = quantity;
        }

        BigDecimal valueSek() { return money(unitPriceSek.multiply(quantity)); }
    }

    public Calculation calculate(BigDecimal cashBalanceSek, List<ValuedHolding> holdings,
                                 List<Quote> approved, Map<AssetCategory, BigDecimal> targets) {
        if (targets.isEmpty()) throw new BusinessRuleException("Configure account allocation targets first");
        BigDecimal sum = targets.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(HUNDRED) != 0) throw new BusinessRuleException("Account targets must sum to 100%");
        if (cashBalanceSek == null || cashBalanceSek.signum() < 0)
            throw new BusinessRuleException("Invalid account cash balance");

        // Holdings remain part of the portfolio even if the user has not approved them.
        Map<Integer, Position> positions = new TreeMap<>();
        for (ValuedHolding holding : holdings) {
            if (holding.instrumentId() == null || holding.quantity() == null || holding.quantity().signum() < 0)
                throw new BusinessRuleException("Invalid instrument link or holding quantity");
            BigDecimal unitPriceSek = holding.currentPrice().multiply(holding.fxRateToSek());
            if (unitPriceSek.signum() <= 0) throw new BusinessRuleException("Missing positive market price");
            var category = AssetCategory.fromInstrumentType(holding.instrumentType());
            var existing = positions.get(holding.instrumentId());
            if (existing == null) {
                positions.put(holding.instrumentId(), new Position(holding.instrumentId(), holding.ticker(), holding.instrumentName(),
                        category, unitPriceSek, holding.quantity()));
            } else {
                if (existing.unitPriceSek.compareTo(unitPriceSek) != 0)
                    throw new BusinessRuleException("Conflicting market prices for duplicate holdings");
                existing.quantity = existing.quantity.add(holding.quantity());
            }
        }
        BigDecimal cashBefore = money(cashBalanceSek);
        EnumMap<AssetCategory, BigDecimal> beforeAmounts = totals(positions.values(), cashBefore);
        BigDecimal total = sum(beforeAmounts.values());
        if (total.signum() <= 0) throw new BusinessRuleException("Account needs holdings or cash to rebalance");
        List<RebalanceAllocationRow> before = rows(beforeAmounts, total, targets);

        // An approved instrument can be newly purchased; a position not in this set must stay untouched.
        Map<Integer, Quote> quotes = new TreeMap<>();
        for (Quote quote : approved) {
            if (quote.unitPriceSek().signum() <= 0 || quote.priceNative().signum() <= 0)
                throw new BusinessRuleException("Instrument price must be positive");
            var type = quote.instrument().getInstrumentType();
            if (type == InstrumentType.OPTION || type == InstrumentType.UNKNOWN)
                throw new BusinessRuleException("Unsupported instrument type for simulated trades");
            quotes.put(quote.instrument().getId(), quote);
            positions.putIfAbsent(quote.instrument().getId(),
                    new Position(quote.instrument().getId(), quote.instrument().getTicker(),
                            quote.instrument().getName(), AssetCategory.fromInstrumentType(type), quote.unitPriceSek(), BigDecimal.ZERO));
        }

        Map<Integer, BigDecimal> initialQuantities = new TreeMap<>();
        for (Position position : positions.values()) {
            initialQuantities.put(position.instrumentId, position.quantity);
        }
        List<RebalanceTradeResponse> trades = new ArrayList<>();
        BigDecimal cash = cashBefore;

        // SELL: reduce excess allocation using only positions the user approved.
        for (AssetCategory category : List.of(AssetCategory.STOCK, AssetCategory.FUND, AssetCategory.BOND)) {
            var current = totals(positions.values(), cash);
            BigDecimal surplus = current.get(category).subtract(targetValue(total, targets, category));
            if (surplus.signum() <= 0) continue;
            for (Quote quote : quotes.values()) {
                Position p = positions.get(quote.instrument().getId());
                if (p.category != category || p.quantity.signum() == 0) continue;
                // Shares are whole; fund/ETF units may be fractional to four decimals.
                BigDecimal qty = floorQuantity(surplus.divide(p.unitPriceSek, 8, RoundingMode.DOWN),
                        quote.instrument().getInstrumentType())
                        .min(floorQuantity(p.quantity, quote.instrument().getInstrumentType()));
                if (qty.signum() <= 0) continue;
                BigDecimal proceeds = money(qty.multiply(p.unitPriceSek));
                if (proceeds.signum() <= 0) continue;
                p.quantity = p.quantity.subtract(qty);
                cash = cash.add(proceeds);
                surplus = surplus.subtract(proceeds);
                trades.add(trade("SELL", p, qty, proceeds));
                if (surplus.signum() <= 0) break;
            }
        }

        // BUY: use available cash (including sale proceeds) for underweight categories.
        // Cash itself counts as OTHER; reserve any cash needed to fulfil the OTHER target.
        for (AssetCategory category : List.of(AssetCategory.STOCK, AssetCategory.FUND, AssetCategory.BOND)) {
            var current = totals(positions.values(), cash);
            BigDecimal deficit = targetValue(total, targets, category).subtract(current.get(category));
            if (deficit.signum() <= 0) continue;
            BigDecimal otherHoldings = current.get(AssetCategory.OTHER).subtract(cash);
            BigDecimal requiredCash = targetValue(total, targets, AssetCategory.OTHER)
                    .subtract(otherHoldings).max(BigDecimal.ZERO);
            BigDecimal spendable = cash.subtract(requiredCash).max(BigDecimal.ZERO);
            BigDecimal budget = deficit.min(spendable);
            if (budget.signum() <= 0) continue;
            List<Quote> candidates = quotes.values().stream()
                    .filter(q -> AssetCategory.fromInstrumentType(q.instrument().getInstrumentType()) == category)
                    .toList();
            for (int i = 0; i < candidates.size(); i++) {
                Quote quote = candidates.get(i);
                Position p = positions.get(quote.instrument().getId());
                BigDecimal budgetPart = budget.divide(BigDecimal.valueOf(candidates.size() - i), 8, RoundingMode.DOWN);
                BigDecimal qty = floorQuantity(budgetPart.divide(p.unitPriceSek, 8, RoundingMode.DOWN),
                        quote.instrument().getInstrumentType());
                if (qty.signum() <= 0) continue;
                BigDecimal cost = money(qty.multiply(p.unitPriceSek));
                if (cost.signum() <= 0 || cost.compareTo(cash) > 0) continue;
                p.quantity = p.quantity.add(qty);
                cash = cash.subtract(cost);
                budget = budget.subtract(cost);
                trades.add(trade("BUY", p, qty, cost));
            }
        }

        EnumMap<AssetCategory, BigDecimal> afterAmounts = totals(positions.values(), cash);
        List<RebalanceAllocationRow> after = rows(afterAmounts, total, targets);
        List<String> warnings = new ArrayList<>();
        BigDecimal originalDrift = before.stream().map(r -> r.differencePctPoints().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal finalDrift = after.stream().map(r -> r.differencePctPoints().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!trades.isEmpty() && finalDrift.compareTo(originalDrift) >= 0) {
            trades.clear();
            for (Position position : positions.values()) {
                position.quantity = initialQuantities.get(position.instrumentId);
            }
            cash = cashBefore;
            after = before;
            warnings.add("No improvement is possible with the approved instrument selection");
        }
        if (trades.isEmpty()) warnings.add("No executable trades for the selected instruments and targets");
        if (after.stream().anyMatch(r -> r.differencePctPoints().abs().compareTo(AllocationRules.DRIFT_THRESHOLD_PERCENTAGE_POINTS) > 0))
            warnings.add("Target allocation could not be fully reached with approved instruments, available funds and unit rounding");
        warnings.add("Simulation prices may come from configured fallback quotes; no real orders are placed");
        List<RebalanceProposedHoldingResponse> proposedHoldings = new ArrayList<>();
        for (Position position : positions.values()) {
            BigDecimal originalQuantity = initialQuantities.get(position.instrumentId);
            if (originalQuantity.signum() == 0 && position.quantity.signum() == 0) continue;
            proposedHoldings.add(new RebalanceProposedHoldingResponse(
                    position.instrumentId, position.ticker, position.instrumentName, position.category,
                    originalQuantity, position.quantity, money(position.unitPriceSek),
                    money(originalQuantity.multiply(position.unitPriceSek)), position.valueSek()));
        }
        return new Calculation(total, cashBefore, money(cash), !trades.isEmpty(), List.copyOf(before),
                List.copyOf(after), List.copyOf(trades), List.copyOf(proposedHoldings), List.copyOf(warnings));
    }

    private static RebalanceTradeResponse trade(String side, Position p, BigDecimal qty, BigDecimal amount) {
        return new RebalanceTradeResponse(side, p.instrumentId, p.ticker, p.category, qty,
                money(p.unitPriceSek), amount);
    }

    private static BigDecimal floorQuantity(BigDecimal quantity, InstrumentType type) {
        int decimals = (type == InstrumentType.FUND || type == InstrumentType.ETF) ? 4 : 0;
        return quantity.setScale(decimals, RoundingMode.DOWN);
    }

    private static EnumMap<AssetCategory, BigDecimal> totals(Collection<Position> positions, BigDecimal cash) {
        EnumMap<AssetCategory, BigDecimal> values = new EnumMap<>(AssetCategory.class);
        for (AssetCategory c : AssetCategory.values()) values.put(c, BigDecimal.ZERO);
        for (Position p : positions) values.merge(p.category, p.valueSek(), BigDecimal::add);
        values.merge(AssetCategory.OTHER, cash, BigDecimal::add);
        return values;
    }

    private static List<RebalanceAllocationRow> rows(Map<AssetCategory, BigDecimal> values,
                                                     BigDecimal total, Map<AssetCategory, BigDecimal> targets) {
        List<RebalanceAllocationRow> result = new ArrayList<>();
        for (AssetCategory category : AssetCategory.values()) {
            BigDecimal value = money(values.getOrDefault(category, BigDecimal.ZERO));
            BigDecimal actual = total.signum() == 0 ? BigDecimal.ZERO :
                    value.multiply(HUNDRED).divide(total, 2, RoundingMode.HALF_UP);
            BigDecimal target = targets.getOrDefault(category, BigDecimal.ZERO);
            result.add(new RebalanceAllocationRow(category, value, actual, target,
                    actual.subtract(target).setScale(2, RoundingMode.HALF_UP)));
        }
        return result;
    }
    private static BigDecimal targetValue(BigDecimal total, Map<AssetCategory, BigDecimal> targets,
                                          AssetCategory category) {
        return total.multiply(targets.getOrDefault(category, BigDecimal.ZERO))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }
    private static BigDecimal sum(Collection<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private static BigDecimal money(BigDecimal number) { return number.setScale(2, RoundingMode.HALF_UP); }
}
