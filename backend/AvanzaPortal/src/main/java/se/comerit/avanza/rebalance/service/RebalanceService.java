package se.comerit.avanza.rebalance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.account.repository.AccountRepository;
import se.comerit.avanza.accountallocation.model.AccountAllocationTarget;
import se.comerit.avanza.accountallocation.model.AssetCategory;
import se.comerit.avanza.accountallocation.repository.AccountAllocationTargetRepository;
import se.comerit.avanza.exception.BusinessRuleException;
import se.comerit.avanza.exception.ResourceConflictException;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.holding.model.Holding;
import se.comerit.avanza.holding.repository.HoldingRepository;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.instrument.repository.InstrumentRepository;
import se.comerit.avanza.market.service.MarketDataService;
import se.comerit.avanza.portfolio.dto.ValuedHolding;
import se.comerit.avanza.portfolio.service.PortfolioValuationService;
import se.comerit.avanza.rebalance.dto.*;

@Service
public class RebalanceService {
    private final AccountRepository accounts;
    private final HoldingRepository holdings;
    private final AccountAllocationTargetRepository targets;
    private final InstrumentRepository instruments;
    private final MarketDataService prices;
    private final PortfolioValuationService valuation;
    private final RebalanceCalculator calculator;
    private final RebalanceProposalTokenService tokens;

    public RebalanceService(AccountRepository accounts, HoldingRepository holdings,
                            AccountAllocationTargetRepository targets, InstrumentRepository instruments,
                            MarketDataService prices, PortfolioValuationService valuation,
                            RebalanceCalculator calculator, RebalanceProposalTokenService tokens) {
        this.accounts = accounts;
        this.holdings = holdings;
        this.targets = targets;
        this.instruments = instruments;
        this.prices = prices;
        this.valuation = valuation;
        this.calculator = calculator;
        this.tokens = tokens;
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional(readOnly = true)
    public RebalancePreviewResponse preview(int accountId, int userId, RebalancePreviewRequest request) {
        Account account = accounts.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        List<Integer> ids = validateIds(request.allowedInstrumentIds());
        Snapshot snapshot = snapshot(account, userId, ids, false);
        RebalanceCalculator.Calculation calculation = calculator.calculate(account.getCashBalanceSek(),
                snapshot.valued(), snapshot.quotes(), snapshot.targets());
        String token = calculation.executable()
                ? tokens.issue(userId, accountId, account.getRebalanceVersion(), snapshot.fingerprint(), ids)
                : null;
        return new RebalancePreviewResponse(accountId, token, calculation.totalValueSek(),
                calculation.cashBeforeSek(), calculation.cashAfterSek(), calculation.executable(),
                calculation.before(), calculation.after(), calculation.trades(), calculation.warnings());
    }

    @PreAuthorize("#userId == authentication.details")
    @CacheEvict(value = "holdingsByUser", allEntries = true)
    @Transactional
    public RebalanceConfirmResponse confirm(int accountId, int userId, RebalanceConfirmRequest request) {
        var proposal = tokens.verify(request.proposalToken(), userId, accountId);
        Account account = accounts.findOwnedAccountForUpdate(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (account.getRebalanceVersion() != proposal.version())
            throw new ResourceConflictException("Proposal already executed or superseded");
        Snapshot snapshot = snapshot(account, userId, proposal.instrumentIds(), true);
        if (!MessageDigest.isEqual(snapshot.fingerprint().getBytes(StandardCharsets.UTF_8),
                proposal.snapshotHash().getBytes(StandardCharsets.UTF_8)))
            throw new ResourceConflictException("Holdings, prices or targets changed; request a fresh preview");
        RebalanceCalculator.Calculation calculation = calculator.calculate(account.getCashBalanceSek(),
                snapshot.valued(), snapshot.quotes(), snapshot.targets());
        if (!calculation.executable())
            throw new ResourceConflictException("No executable trades remain");

        Map<Integer, Holding> ownedPositions = new HashMap<>();
        for (Holding h : snapshot.entities()) {
            if (h.getInstrument() == null) throw new BusinessRuleException("Legacy unlinked holding");
            Holding old = ownedPositions.put(h.getInstrument().getId(), h);
            if (old != null && proposal.instrumentIds().contains(h.getInstrument().getId()))
                throw new BusinessRuleException("Merge duplicate holdings for an approved instrument first");
        }
        Map<Integer, Instrument> approvedInstruments = new HashMap<>();
        for (var quote : snapshot.quotes()) approvedInstruments.put(quote.instrument().getId(), quote.instrument());
        for (RebalanceTradeResponse trade : calculation.trades()) {
            Holding holding = ownedPositions.get(trade.instrumentId());
            Instrument instrument = approvedInstruments.get(trade.instrumentId());
            if (instrument == null) throw new BusinessRuleException("Trade instrument not approved");
            if (trade.side().equals("SELL")) {
                if (holding == null || holding.getQuantity().compareTo(trade.quantity()) < 0)
                    throw new ResourceConflictException("Insufficient holding quantity");
                BigDecimal newQuantity = holding.getQuantity().subtract(trade.quantity());
                if (newQuantity.signum() == 0) {
                    holdings.delete(holding);
                    ownedPositions.remove(trade.instrumentId());
                } else {
                    holding.setQuantity(newQuantity);
                    holdings.save(holding);
                }
            } else if (trade.side().equals("BUY")) {
                BigDecimal nativePrice = snapshot.quotes().stream()
                        .filter(q -> q.instrument().getId().equals(trade.instrumentId()))
                        .findFirst().orElseThrow().priceNative();
                if (holding == null) {
                    holding = new Holding(accountId, instrument, trade.quantity(), nativePrice.setScale(2, RoundingMode.HALF_UP));
                    holdings.save(holding);
                    ownedPositions.put(trade.instrumentId(), holding);
                } else {
                    BigDecimal oldQty = holding.getQuantity();
                    BigDecimal newQty = oldQty.add(trade.quantity());
                    BigDecimal newAverage = holding.getAvgBuyPrice().multiply(oldQty)
                            .add(nativePrice.multiply(trade.quantity()))
                            .divide(newQty, 2, RoundingMode.HALF_UP);
                    holding.setQuantity(newQty);
                    holding.setAvgBuyPrice(newAverage);
                    holdings.save(holding);
                }
            } else throw new BusinessRuleException("Unsupported trade direction");
        }
        account.setCashBalanceSek(calculation.cashAfterSek());
        account.setRebalanceVersion(account.getRebalanceVersion() + 1);
        accounts.save(account);
        return new RebalanceConfirmResponse(accountId, "EXECUTED", calculation.cashAfterSek(), calculation.trades());
    }

    private Snapshot snapshot(Account account, int userId, List<Integer> ids, boolean forUpdate) {
        int accountId = account.getId();
        List<Holding> entities = forUpdate ? holdings.findAllByOwnedAccountForUpdate(accountId, userId)
                : holdings.findAllByOwnedAccount(accountId, userId);
        // A simple simulator cannot safely pick which of two separate holding rows to sell.
        // Reject this clearly at preview time instead of failing only at confirm time.
        Set<Integer> seenApproved = new HashSet<>();
        for (Holding holding : entities) {
            if (holding.getInstrument() != null && ids.contains(holding.getInstrument().getId())
                    && !seenApproved.add(holding.getInstrument().getId()))
                throw new BusinessRuleException("Merge duplicate holdings for an approved instrument first");
        }
        List<ValuedHolding> valued = entities.stream().map(valuation::value).toList();
        List<AccountAllocationTarget> targetRows = targets.findByAccountIdOrderByAssetCategoryAsc(accountId);
        if (targetRows.isEmpty()) throw new BusinessRuleException("Set account allocation targets before rebalancing");
        EnumMap<AssetCategory, BigDecimal> targetMap = new EnumMap<>(AssetCategory.class);
        for (AccountAllocationTarget target : targetRows)
            targetMap.put(target.getAssetCategory(), target.getTargetPct());
        List<Instrument> matched = instruments.findAllById(ids);
        if (matched.size() != ids.size()) throw new BusinessRuleException("One or more approved instrument IDs do not exist");
        List<RebalanceCalculator.Quote> quotes = matched.stream()
                .sorted(Comparator.comparing(Instrument::getId))
                .map(i -> new RebalanceCalculator.Quote(i, prices.getPrice(i.getTicker()),
                        prices.getEffectiveFxRate(i.getCurrency(), "SEK")))
                .toList();
        StringBuilder data = new StringBuilder("rb1:").append(accountId).append(':')
                .append(account.getRebalanceVersion()).append(':').append(canonical(account.getCashBalanceSek()));
        targetMap.forEach((k, v) -> data.append("|target:").append(k).append(':').append(canonical(v)));
        valued.stream().sorted(Comparator.comparing(ValuedHolding::holdingId)).forEach(h -> data
                .append("|holding:").append(h.holdingId()).append(':').append(h.instrumentId())
                .append(':').append(canonical(h.quantity())).append(':').append(canonical(h.avgBuyPrice()))
                .append(':').append(canonical(h.currentPrice())).append(':').append(canonical(h.fxRateToSek()))
                .append(':').append(h.instrumentType()));
        for (var q : quotes) data.append("|allowed:").append(q.instrument().getId())
                .append(':').append(q.instrument().getInstrumentType())
                .append(':').append(canonical(q.priceNative())).append(':').append(canonical(q.fxToSek()));
        return new Snapshot(List.copyOf(entities), valued, quotes, targetMap, sha256(data.toString()));
    }

    private static List<Integer> validateIds(List<Integer> values) {
        if (values == null || values.isEmpty() || values.size() > 50 ||
                values.stream().anyMatch(v -> v == null || v <= 0))
            throw new BusinessRuleException("Select 1–50 valid approved instrument IDs");
        List<Integer> ids = values.stream().distinct().sorted().toList();
        if (ids.size() != values.size()) throw new BusinessRuleException("Duplicate instrument ID");
        return ids;
    }

    private static String canonical(BigDecimal number) {
        return number.stripTrailingZeros().toPlainString();
    }

    private static String sha256(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private record Snapshot(List<Holding> entities, List<ValuedHolding> valued,
                            List<RebalanceCalculator.Quote> quotes,
                            Map<AssetCategory, BigDecimal> targets, String fingerprint) {}
}
