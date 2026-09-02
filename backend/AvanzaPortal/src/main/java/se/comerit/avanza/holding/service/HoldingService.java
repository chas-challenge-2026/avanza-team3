package se.comerit.avanza.holding.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.comerit.avanza.account.service.AccountService;
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

    @Cacheable(value = "holdingsByUser", key = "#userId")
    @Transactional
    public List<Map<String, Object>> getHoldingsByUserId(Integer userId) {

        List<Holding> holdings = holdingRepository.findByAccountUserIdOrderByAccountAccountTypeAscTickerAsc(userId);

        Map<String, BigDecimal> prices = createPriceMap();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Holding holding : holdings) {
            result.add(toHoldingMap(holding, prices));
        }

        return result;
    }

    @Transactional
    @Cacheable(value = "holdingsByAccount", key = "#userId + '-' + #accountId + '-' + #page + '-' + #size")
    public Page<Map<String, Object>> getHoldingsByAccountIdAndUserId(Integer accountId, Integer userId, int page, int size) {
        accountService.getAccountByIdAndUserId(accountId, userId);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.asc("ticker")));

        Page<Holding> holdings =
                holdingRepository.findByAccountIdAndAccountUserId(
                accountId,
                userId,
                pageable
                    );

        Map<String, BigDecimal> prices = createPriceMap();
        return holdings.map(holding -> toHoldingMap(holding, prices));
    }

    public List<Map<String, Object>> getAccountsByUserId(Integer userId) {
        return accountService.getAccountMapsByUserId(userId);
    }

    @Transactional
    @CacheEvict(value = {"holdingsByUser", "holdingsByAccount"}, allEntries = true)
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

    @Transactional
    @CacheEvict(value = {"holdingsByUser", "holdingsByAccount"}, allEntries = true)
    public void deleteHolding(Integer holdingId, Integer userId)
    {
        Holding holdingToDelete = holdingRepository
                .findByIdAndAccountUserId(holdingId, userId)
                        .orElseThrow(() -> new IllegalArgumentException("Holding not Found"));
        holdingRepository.delete(holdingToDelete);
    }

    private Map<String, BigDecimal> createPriceMap() {
        Map<String, BigDecimal> prices = new HashMap<>();
        prices.put("ERIC-B", new BigDecimal("74.20"));
        prices.put("VOLV-B", new BigDecimal("268.50"));
        prices.put("AAPL", new BigDecimal("187.32"));
        prices.put("SWED-A", new BigDecimal("193.10"));
        prices.put("SAND", new BigDecimal("212.80"));
        return prices;
    }

    private Map<String, Object> toHoldingMap(
            Holding holding,
            Map<String, BigDecimal> prices) {

        BigDecimal currentPrice = prices.getOrDefault(holding.getTicker(), BigDecimal.ZERO);
        BigDecimal quantity = holding.getQuantity() != null ? holding.getQuantity() : BigDecimal.ZERO;
        BigDecimal avgBuyPrice = holding.getAvgBuyPrice() != null ? holding.getAvgBuyPrice() : BigDecimal.ZERO;
        BigDecimal marketValue = quantity.multiply(currentPrice);
        BigDecimal costBasis = quantity.multiply(avgBuyPrice);
        BigDecimal pnl = marketValue.subtract(costBasis);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", holding.getId());
        response.put("account_id", holding.getAccountId());
        response.put("ticker", holding.getTicker());
        response.put("instrument_name", holding.getInstrumentName());
        response.put("quantity", holding.getQuantity());
        response.put("avg_buy_price", holding.getAvgBuyPrice());
        response.put("currency", holding.getCurrency());
        response.put("account_type", holding.getAccount().getAccountType());
        response.put("account_name", holding.getAccount().getAccountName());
        response.put("currentPrice", currentPrice.setScale(2, RoundingMode.HALF_UP));
        response.put("marketValue", marketValue.setScale(2, RoundingMode.HALF_UP));
        response.put("pnl", pnl.setScale(2, RoundingMode.HALF_UP));
        return response;
    }
}
