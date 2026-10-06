package se.comerit.avanza.holding.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import se.comerit.avanza.account.service.AccountService;
import se.comerit.avanza.holding.dto.HoldingPatchRequest;
import se.comerit.avanza.holding.dto.HoldingResponse;
import se.comerit.avanza.holding.model.Holding;
import se.comerit.avanza.holding.repository.HoldingRepository;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.service.InstrumentService;
import se.comerit.avanza.market.service.MarketDataService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final AccountService accountService;
    private final MarketDataService marketDataService;
    private final InstrumentService instrumentService;

    public HoldingService(HoldingRepository holdingRepository, AccountService accountService, MarketDataService marketDataService, InstrumentService instrumentService) {
        this.holdingRepository = holdingRepository;
        this.accountService = accountService;
        this.marketDataService = marketDataService;
        this.instrumentService = instrumentService;
    }

    @PreAuthorize("#userId == authentication.details")
    @Cacheable(value = "holdingsByUser", key = "#userId")
    @Transactional
    public List<Map<String, Object>> getHoldingsByUserId(Integer userId) {

        List<Holding> holdings = holdingRepository.findByAccountUserIdOrderByAccountAccountTypeAscInstrumentTickerAsc(userId);

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

        Page<Holding> holdings =
                holdingRepository.findByAccountUserId(userId, pageable);

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
            InstrumentType instrumentType
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.asc("account.accountType"),
                        Sort.Order.asc("instrument.ticker")));

        Page<Holding> holdings =
                holdingRepository.findFilteredByUserId(
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
            InstrumentType instrumentType
    ) {
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
    public HoldingResponse updateHolding(Integer holdingId, Integer userId, HoldingPatchRequest request) {
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
    public void addHolding(Integer userId, Integer accountId,
            Integer instrumentId,
            BigDecimal quantity, BigDecimal avgBuyPrice) {
        accountService.getAccountByIdAndUserId(accountId, userId);
        Instrument instrument = instrumentService.getById(instrumentId);

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
    public void deleteHolding(Integer holdingId, Integer userId)
    {
        Holding holdingToDelete = holdingRepository
                .findByIdAndAccountUserId(holdingId, userId)
                        .orElseThrow(() -> new IllegalArgumentException("Holding not Found"));
        holdingRepository.delete(holdingToDelete);
    }

    private HoldingResponse toHoldingResponse(Holding holding) {

        Instrument instrument = requireInstrument(holding);
        HoldingValues values =
                calculateHoldingValues(holding, instrument);

        return new HoldingResponse(
                holding.getId(),
                holding.getAccountId(),
                instrument.getId(),
                instrument.getTicker(),
                instrument.getName(),
                instrument.getInstrumentType(),
                instrument.getSector(),
                holding.getQuantity(),
                holding.getAvgBuyPrice(),
                instrument.getCurrency(),
                values.currentPrice(),
                values.marketValue(),
                values.pnl(),
                values.pnlPct()
        );
    }

    private Holding getOwnedHolding(Integer holdingId, Integer userId) {
        return holdingRepository.findByIdAndAccountUserId(holdingId, userId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Holding not found"));
    }

    private Instrument requireInstrument(Holding holding) {
        if (holding.getInstrument() == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Holding " + holding.getId() + " is not linked to an instrument"
            );
        }
        return holding.getInstrument();
    }

    private record HoldingValues(
            BigDecimal currentPrice,
            BigDecimal marketValue,
            BigDecimal pnl,
            BigDecimal pnlPct
    ) {}

    private HoldingValues calculateHoldingValues(Holding holding, Instrument instrument) {

        BigDecimal currentPrice =
                marketDataService.getPrice(instrument.getTicker());

        BigDecimal quantity = holding.getQuantity() != null
                ? holding.getQuantity()
                : BigDecimal.ZERO;

        BigDecimal avgBuyPrice = holding.getAvgBuyPrice() != null
                ? holding.getAvgBuyPrice()
                : BigDecimal.ZERO;

        BigDecimal marketValue =
                quantity.multiply(currentPrice);

        BigDecimal costBasis =
                quantity.multiply(avgBuyPrice);

        BigDecimal pnl =
                marketValue.subtract(costBasis);

        BigDecimal pnlPct = costBasis.compareTo(BigDecimal.ZERO) > 0
                ? pnl.divide(costBasis, 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return new HoldingValues(
                currentPrice.setScale(2, RoundingMode.HALF_UP),
                marketValue.setScale(2, RoundingMode.HALF_UP),
                pnl.setScale(2, RoundingMode.HALF_UP),
                pnlPct.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private Map<String, Object> toHoldingMap(Holding holding) {

        Instrument instrument = requireInstrument(holding);

        HoldingValues values =
                calculateHoldingValues(holding, instrument);

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("id", holding.getId());
        result.put("account_id", holding.getAccountId());
        result.put("instrument_id", instrument.getId());
        result.put("ticker", instrument.getTicker());
        result.put("instrument_name", instrument.getName());
        result.put("instrument_type", instrument.getInstrumentType());
        result.put("sector", instrument.getSector());
        result.put("quantity", holding.getQuantity());
        result.put("avg_buy_price", holding.getAvgBuyPrice());
        result.put("currency", instrument.getCurrency());
        result.put("account_type", holding.getAccount().getAccountType());
        result.put("account_name", holding.getAccount().getAccountName());
        result.put("currentPrice", values.currentPrice());
        result.put("marketValue", values.marketValue());
        result.put("pnl", values.pnl());
        result.put("pnlPct", values.pnlPct());

        return result;
    }
}
