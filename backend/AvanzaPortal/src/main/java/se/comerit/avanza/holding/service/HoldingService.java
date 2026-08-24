package se.comerit.avanza.holding.service;

import org.springframework.stereotype.Service;
import se.comerit.avanza.holding.repository.HoldingRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HoldingService {

    private final HoldingRepository holdingRepository;

    public HoldingService(HoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    public List<Map<String, Object>> getHoldingsByUserId(Integer userId) {

        List<Map<String, Object>> holdings = holdingRepository.findHoldingsByUserId(userId);

        Map<String, Double> prices = new HashMap<>();
        prices.put("ERIC-B", 74.20);
        prices.put("VOLV-B", 268.50);
        prices.put("AAPL", 187.32);
        prices.put("SWED-A", 193.10);
        prices.put("SAND", 212.80);

        for (Map<String, Object> h : holdings) {
            String ticker = (String) h.get("ticker");
            double currentPrice = prices.getOrDefault(ticker, 0.0);
            double qty = ((java.math.BigDecimal) h.get("quantity")).doubleValue();
            double avgBuy = ((java.math.BigDecimal) h.get("avg_buy_price")).doubleValue();
            double marketValue = qty * currentPrice;
            double costBasis = qty * avgBuy;
            double pnl = marketValue - costBasis;

            h.put("currentPrice", currentPrice);
            h.put("marketValue", Math.round(marketValue * 100.0) / 100.0);
            h.put("pnl", Math.round(pnl * 100.0) / 100.0);
        }

        return holdings;
    }

    public List<Map<String, Object>> getAccountsByUserId(Integer userId) {
        return holdingRepository.findHoldingsByUserId(userId);
    }
}
