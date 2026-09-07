package se.comerit.avanza.market.provider;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Component
public class FallbackMarketDataProvider implements MarketDataProvider {

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("100.00");
    private static final Map<String, BigDecimal> PRICE_FALLBACKS = Map.of(
            "ERIC-B", new BigDecimal("74.20"),
            "VOLV-B", new BigDecimal("268.50"),
            "AAPL", new BigDecimal("187.32"),
            "SWED-A", new BigDecimal("193.10"),
            "SAND", new BigDecimal("212.80")
    );
    private static final Map<String, BigDecimal> FX_FALLBACKS = Map.of(
            "USD/SEK", new BigDecimal("10.45"),
            "SEK/SEK", BigDecimal.ONE
    );

    @Override
    public Optional<BigDecimal> findPrice(String ticker) {
        return Optional.of(PRICE_FALLBACKS.getOrDefault(ticker.toUpperCase(), DEFAULT_PRICE));
    }

    @Override
    public Optional<BigDecimal> findFxRate(String fromCurrency, String toCurrency) {
        String from = fromCurrency.toUpperCase();
        String to = toCurrency.toUpperCase();
        if (from.equals(to)) {
            return Optional.of(BigDecimal.ONE);
        }

        return Optional.ofNullable(FX_FALLBACKS.get(from + "/" + to));
    }
}
