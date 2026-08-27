package se.comerit.avanza.holding.service;

import org.springframework.stereotype.Service;
import se.comerit.avanza.holding.model.Holding;
import se.comerit.avanza.holding.repository.HoldingRepository;
import se.comerit.avanza.holding.repository.TempHoldingJdbcRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final TempHoldingJdbcRepository tempHoldingJdbcRepository;

    public HoldingService(HoldingRepository holdingRepository, TempHoldingJdbcRepository tempHoldingJdbcRepository) {
        this.holdingRepository = holdingRepository;
        this.tempHoldingJdbcRepository = tempHoldingJdbcRepository;
    }

    public List<Map<String, Object>> getHoldingsByUserId(Integer userId) {

        List<Holding> holdings = holdingRepository.findByAccountUserIdOrderByAccountAccountTypeAscTickerAsc(userId);

        Map<String, Double> prices = new HashMap<>();
        prices.put("ERIC-B", 74.20);
        prices.put("VOLV-B", 268.50);
        prices.put("AAPL", 187.32);
        prices.put("SWED-A", 193.10);
        prices.put("SAND", 212.80);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Holding holding : holdings) {
            double currentPrice = prices.getOrDefault(holding.getTicker(), 0.0);
            double qty = holding.getQuantity() != null ? holding.getQuantity() : 0.0;
            double avgBuy = holding.getAvgBuyPrice() != null ? holding.getAvgBuyPrice() : 0.0;
            double marketValue = qty * currentPrice;
            double costBasis = qty * avgBuy;
            double pnl = marketValue - costBasis;

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
            h.put("currentPrice", currentPrice);
            h.put("marketValue", Math.round(marketValue * 100.0) / 100.0);
            h.put("pnl", Math.round(pnl * 100.0) / 100.0);
            result.add(h);
        }

        return result;
    }

    public List<Map<String, Object>> getAccountsByUserId(Integer userId) {
        return tempHoldingJdbcRepository.findAccountsByUserId(userId);
    }

    public void addHolding(Integer accountId, String ticker, String instrumentName, String quantity, String avgBuyPrice, String currency) {

        Holding holding = new Holding(
                accountId,
                ticker,
                instrumentName,
                Double.valueOf(quantity),
                Double.valueOf(avgBuyPrice),
                currency
        );

        holdingRepository.save(holding);
    }

    public void deleteHolding(Integer holdingId) {
        holdingRepository.deleteById(holdingId);
    }
}
