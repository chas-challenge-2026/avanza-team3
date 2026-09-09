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
import se.comerit.avanza.market.service.MarketDataService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final AccountService accountService;
    private final MarketDataService marketDataService;

    public HoldingService(HoldingRepository holdingRepository, AccountService accountService, MarketDataService marketDataService) {
        this.holdingRepository = holdingRepository;
        this.accountService = accountService;
        this.marketDataService = marketDataService;
    }

    @PreAuthorize("#userId == authentication.details")
    @Cacheable(value = "holdingsByUser", key = "#userId")
    @Transactional
    public List<Map<String, Object>> getHoldingsByUserId(Integer userId) {

        List<Holding> holdings = holdingRepository.findByAccountUserIdOrderByAccountAccountTypeAscTickerAsc(userId);

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
                        Sort.Order.asc("ticker")));

        Page<Holding> holdings =
                holdingRepository.findByAccountUserId(userId, pageable);

        return holdings.map(this::toHoldingMap);
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

        if (request.ticker() != null) {
            holding.setTicker(request.ticker());
        }
        if (request.instrumentName() != null) {
            holding.setInstrumentName(request.instrumentName());
        }
        if (request.quantity() != null) {
            holding.setQuantity(request.quantity());
        }
        if (request.avgBuyPrice() != null) {
            holding.setAvgBuyPrice(request.avgBuyPrice());
        }
        if (request.currency() != null) {
            holding.setCurrency(request.currency());
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
    public void addHolding(Integer userId, Integer accountId, String ticker, String instrumentName, BigDecimal quantity, BigDecimal avgBuyPrice, String currency) {

        accountService.getAccountByIdAndUserId(accountId, userId);

        Holding holding = new Holding(
                accountId,
                ticker,
                instrumentName,
                quantity,
                avgBuyPrice,
                currency
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

        HoldingValues values =
                calculateHoldingValues(holding);

        return new HoldingResponse(
                holding.getId(),
                holding.getAccountId(),
                holding.getTicker(),
                holding.getInstrumentName(),
                holding.getQuantity(),
                holding.getAvgBuyPrice(),
                holding.getCurrency(),
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

    private record HoldingValues(
            BigDecimal currentPrice,
            BigDecimal marketValue,
            BigDecimal pnl,
            BigDecimal pnlPct
    ) {}

    private HoldingValues calculateHoldingValues(Holding holding) {

        BigDecimal currentPrice =
                marketDataService.getPrice(holding.getTicker());

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

        HoldingValues values =
                calculateHoldingValues(holding);

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("id", holding.getId());
        result.put("account_id", holding.getAccountId());
        result.put("ticker", holding.getTicker());
        result.put("instrument_name", holding.getInstrumentName());
        result.put("quantity", holding.getQuantity());
        result.put("avg_buy_price", holding.getAvgBuyPrice());
        result.put("currency", holding.getCurrency());
        result.put("account_type", holding.getAccount().getAccountType());
        result.put("account_name", holding.getAccount().getAccountName());
        result.put("currentPrice", values.currentPrice());
        result.put("marketValue", values.marketValue());
        result.put("pnl", values.pnl());
        result.put("pnlPct", values.pnlPct());

        return result;
    }
}
