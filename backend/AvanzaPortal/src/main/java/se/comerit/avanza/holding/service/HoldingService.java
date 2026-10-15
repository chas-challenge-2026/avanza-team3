package se.comerit.avanza.holding.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.comerit.avanza.account.service.AccountService;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.holding.dto.HoldingPatchRequest;
import se.comerit.avanza.holding.dto.HoldingResponse;
import se.comerit.avanza.holding.model.Holding;
import se.comerit.avanza.holding.repository.HoldingRepository;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;
import se.comerit.avanza.instrument.service.InstrumentService;
import se.comerit.avanza.portfolio.dto.ValuedHolding;
import se.comerit.avanza.portfolio.service.PortfolioValuationService;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final AccountService accountService;
    private final PortfolioValuationService portfolioValuationService;
    private final InstrumentService instrumentService;

    public HoldingService(
            HoldingRepository holdingRepository,
            AccountService accountService,
            PortfolioValuationService portfolioValuationService,
            InstrumentService instrumentService) {
        this.holdingRepository = holdingRepository;
        this.accountService = accountService;
        this.portfolioValuationService = portfolioValuationService;
        this.instrumentService = instrumentService;
    }

    @PreAuthorize("#userId == authentication.details")
    @Cacheable(value = "holdingsByUser", key = "#userId")
    @Transactional
    public List<Map<String, Object>> getHoldingsByUserId(Integer userId) {
        List<Holding> holdings = holdingRepository
                .findByAccountUserIdOrderByAccountAccountTypeAscInstrumentTickerAsc(userId);

        return holdings.stream()
                .map(this::toHoldingMap)
                .toList();
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    @Cacheable(value = "holdingsByUser", key = "#userId + '-' + #page + '-' + #size")
    public Page<Map<String, Object>> getHoldingsByUserId(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.asc("account.accountType"),
                        Sort.Order.asc("instrument.ticker")));

        Page<Holding> holdings = holdingRepository.findByAccountUserId(userId, pageable);
        return holdings.map(this::toHoldingMap);
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    @Cacheable(
            value = "holdingsByUser",
            key = "#userId + '-' + #page + '-' + #size + '-' + #accountId + '-' + #instrumentType"
    )
    public Page<Map<String, Object>> getHoldingsByUserId(
            Integer userId,
            int page,
            int size,
            Integer accountId,
            InstrumentType instrumentType) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.asc("account.accountType"),
                        Sort.Order.asc("instrument.ticker")));

        Page<Holding> holdings = holdingRepository.findFilteredByUserId(
                userId,
                accountId,
                instrumentType,
                pageable
        );

        return holdings.map(this::toHoldingMap);
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional(readOnly = true)
    @Cacheable(
            value = "holdingsByUser",
            key = "'summary-' + #userId + '-' + #accountId + '-' + #instrumentType"
    )
    public List<Map<String, Object>> getHoldingsSummary(
            Integer userId,
            Integer accountId,
            InstrumentType instrumentType) {

        List<Holding> holdings = holdingRepository.findAllFilteredByUserId(
                userId,
                accountId,
                instrumentType
        );

        return holdings.stream()
                .map(this::toHoldingMap)
                .toList();
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    public HoldingResponse getHoldingById(Integer holdingId, Integer userId) {
        Holding holding = getOwnedHolding(holdingId, userId);
        return toHoldingResponse(holding);
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    @CacheEvict(value = "holdingsByUser", allEntries = true)
    public HoldingResponse updateHolding(
            Integer holdingId,
            Integer userId,
            HoldingPatchRequest request) {

        Holding holding = getOwnedHolding(holdingId, userId);

        if (request.quantity() != null) {
            holding.setQuantity(request.quantity());
        }
        if (request.avgBuyPrice() != null) {
            holding.setAvgBuyPrice(request.avgBuyPrice());
        }

        Holding updatedHolding = holdingRepository.save(holding);
        return toHoldingResponse(updatedHolding);
    }

    @PreAuthorize("#userId == authentication.details")
    public List<Map<String, Object>> getAccountsByUserId(Integer userId) {
        return accountService.getAccountMapsByUserId(userId);
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    @CacheEvict(value = "holdingsByUser", allEntries = true)
    public void addHolding(
            Integer userId,
            Integer accountId,
            String ticker,
            BigDecimal quantity,
            BigDecimal avgBuyPrice) {

        accountService.getAccountByIdAndUserId(accountId, userId);
        Instrument instrument = instrumentService.getByTicker(ticker);

        Holding holding = new Holding(
                accountId,
                instrument,
                quantity,
                avgBuyPrice
        );

        holdingRepository.save(holding);
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    @CacheEvict(value = "holdingsByUser", allEntries = true)
    public void deleteHolding(Integer holdingId, Integer userId) {
        Holding holdingToDelete = holdingRepository
                .findByIdAndAccountUserId(holdingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Holding not found"));
        holdingRepository.delete(holdingToDelete);
    }

    private HoldingResponse toHoldingResponse(Holding holding) {
        ValuedHolding valued = portfolioValuationService.value(holding);

        return new HoldingResponse(
                valued.holdingId(),
                valued.accountId(),
                valued.instrumentId(),
                valued.ticker(),
                valued.instrumentName(),
                getInstrumentTypeDisplayName(valued),
                getSectorDisplayName(valued),
                valued.quantity(),
                valued.avgBuyPrice(),
                valued.currency(),
                valued.currentPrice(),
                valued.marketValueSek(),
                valued.pnlSek(),
                valued.pnlPct()
        );
    }

    private Holding getOwnedHolding(Integer holdingId, Integer userId) {
        return holdingRepository.findByIdAndAccountUserId(holdingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Holding not found"));
    }

    private Map<String, Object> toHoldingMap(Holding holding) {
        ValuedHolding valued = portfolioValuationService.value(holding);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", valued.holdingId());
        result.put("account_id", valued.accountId());
        result.put("instrument_id", valued.instrumentId());
        result.put("ticker", valued.ticker());
        result.put("instrument_name", valued.instrumentName());
        result.put("instrument_type", getInstrumentTypeDisplayName(valued));
        result.put("sector", getSectorDisplayName(valued));
        result.put("quantity", valued.quantity());
        result.put("avg_buy_price", valued.avgBuyPrice());
        result.put("currency", valued.currency());
        result.put("account_type", valued.accountType());
        result.put("account_name", valued.accountName());
        result.put("currentPrice", valued.currentPrice());
        result.put("marketValue", valued.marketValueSek());
        result.put("pnl", valued.pnlSek());
        result.put("pnlPct", valued.pnlPct());

        return result;
    }

    private String getInstrumentTypeDisplayName(ValuedHolding valued) {
        InstrumentType instrumentType = valued.instrumentType();
        return instrumentType != null ? instrumentType.getDisplayName() : "Övrigt";
    }

    private String getSectorDisplayName(ValuedHolding valued) {
        InstrumentType instrumentType = valued.instrumentType();
        if (instrumentType != null && instrumentType.isFund()) {
            return "Fonder";
        }

        Sector sector = valued.sector();
        return sector != null ? sector.getDisplayName() : "Övrigt";
    }
}
