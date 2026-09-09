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

import javax.swing.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final AccountService accountService;

    public HoldingService(HoldingRepository holdingRepository, AccountService accountService) {
        this.holdingRepository = holdingRepository;
        this.accountService = accountService;
    }

    @PreAuthorize("#userId == authentication.details")
    @Cacheable(value = "holdingsByUser", key = "#userId")
    @Transactional
    public List<Map<String, Object>> getHoldingsByUserId(Integer userId) {

        List<Holding> holdings = holdingRepository.findByAccountUserIdOrderByAccountAccountTypeAscTickerAsc(userId);

        Map<String, BigDecimal> prices = new HashMap<>();
        prices.put("ERIC-B", new BigDecimal("74.20"));
        prices.put("VOLV-B", new BigDecimal("268.50"));
        prices.put("AAPL", new BigDecimal("187.32"));
        prices.put("SWED-A", new BigDecimal("193.10"));
        prices.put("SAND", new BigDecimal("212.80"));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Holding holding : holdings) {
            BigDecimal currentPrice = prices.getOrDefault(holding.getTicker(), BigDecimal.ZERO);
            BigDecimal qty = holding.getQuantity() != null ? holding.getQuantity() : BigDecimal.ZERO;
            BigDecimal avgBuy = holding.getAvgBuyPrice() != null ? holding.getAvgBuyPrice() : BigDecimal.ZERO;
            BigDecimal marketValue = qty.multiply(currentPrice);
            BigDecimal costBasis = qty.multiply(avgBuy);
            BigDecimal pnl = marketValue.subtract(costBasis);

            Map<String, Object> h = new LinkedHashMap<>();
            h.put("id", holding.getId());
            h.put("account_id", holding.getAccountId());
            h.put("ticker", holding.getTicker());
            h.put("instrument_name", holding.getInstrumentName());
            h.put("quantity", holding.getQuantity());
            h.put("avg_buy_price", holding.getAvgBuyPrice());
            h.put("currency", holding.getCurrency());
            h.put("account_type", holding.getAccount().getAccountType());
            h.put("account_name", holding.getAccount().getAccountName());
            h.put("currentPrice", currentPrice.setScale(2, RoundingMode.HALF_UP));
            h.put("marketValue", marketValue.setScale(2, RoundingMode.HALF_UP));
            h.put("pnl", pnl.setScale(2, RoundingMode.HALF_UP));
            result.add(h);
        }

        return result;
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

        Map<String, BigDecimal> prices = new HashMap<>();
        prices.put("ERIC-B", new BigDecimal("74.20"));
        prices.put("VOLV-B", new BigDecimal("268.50"));
        prices.put("AAPL", new BigDecimal("187.32"));
        prices.put("SWED-A", new BigDecimal("193.10"));
        prices.put("SAND", new BigDecimal("212.80"));

        return holdings.map(holding -> {

            BigDecimal currentPrice =
                    prices.getOrDefault(
                            holding.getTicker(),
                            BigDecimal.ZERO
                    );

            BigDecimal qty =
                    holding.getQuantity() != null
                            ? holding.getQuantity()
                            : BigDecimal.ZERO;

            BigDecimal avgBuy =
                    holding.getAvgBuyPrice() != null
                            ? holding.getAvgBuyPrice()
                            : BigDecimal.ZERO;

            BigDecimal marketValue =
                    qty.multiply(currentPrice);

            BigDecimal costBasis =
                    qty.multiply(avgBuy);

            BigDecimal pnl =
                    marketValue.subtract(costBasis);

            Map<String, Object> h = new LinkedHashMap<>();

            h.put("id", holding.getId());
            h.put("account_id", holding.getAccountId());
            h.put("ticker", holding.getTicker());
            h.put("instrument_name", holding.getInstrumentName());
            h.put("quantity", holding.getQuantity());
            h.put("avg_buy_price", holding.getAvgBuyPrice());
            h.put("currency", holding.getCurrency());
            h.put("account_type", holding.getAccount().getAccountType());
            h.put("account_name", holding.getAccount().getAccountName());
            h.put("currentPrice", currentPrice.setScale(2, RoundingMode.HALF_UP));
            h.put("marketValue", marketValue.setScale(2, RoundingMode.HALF_UP));
            h.put("pnl", pnl.setScale(2, RoundingMode.HALF_UP));
            return h;
        });
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    public HoldingResponse getHoldingById(Integer userId, Integer holdingId) {
        Holding holding = getOwnedHolding(holdingId, userId);
        return toHoldingResponse(holding);
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
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
        return new HoldingResponse(
                holding.getId(),
                holding.getAccountId(),
                holding.getTicker(),
                holding.getInstrumentName(),
                holding.getQuantity(),
                holding.getAvgBuyPrice(),
                holding.getCurrency()
        );
    }

    private Holding getOwnedHolding(Integer holdingId, Integer userId) {
        return holdingRepository.findByIdAndAccountUserId(holdingId, userId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Holding not found"));
    }
}
