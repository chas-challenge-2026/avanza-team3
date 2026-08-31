package se.comerit.avanza.holding.service;

import org.springframework.stereotype.Service;
import se.comerit.avanza.account.service.AccountService;
import se.comerit.avanza.holding.model.Holding;
import se.comerit.avanza.holding.repository.HoldingRepository;

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

    public List<Map<String, Object>> getAccountsByUserId(Integer userId) {
        return accountService.getAccountMapsByUserId(userId);
    }

    public void addHolding(Integer accountId, String ticker, String instrumentName, String quantity, String avgBuyPrice, String currency) {

        Holding holding = new Holding(
                accountId,
                ticker,
                instrumentName,
                new BigDecimal(quantity),
                new BigDecimal(avgBuyPrice),
                currency
        );

        holdingRepository.save(holding);
    }

    public void deleteHolding(Integer holdingId, Integer userId)
    {
        Holding holdingToDelete = holdingRepository
                .findByIdAndAccountUserId(holdingId, userId)
                        .orElseThrow(() -> new IllegalArgumentException("Holding not Found"));
        holdingRepository.delete(holdingToDelete);
    }
}
