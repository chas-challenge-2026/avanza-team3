package se.comerit.avanza.market.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import se.comerit.avanza.market.provider.MarketDataProvider;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MarketDataService {

    private final List<MarketDataProvider> providers;

    public MarketDataService(List<MarketDataProvider> providers) {
        this.providers = providers;
    }

    @Cacheable(value = "marketPrices", key = "#ticker.toUpperCase()")
    public BigDecimal getPrice(String ticker) {
        String normalizedTicker = ticker.toUpperCase();
        return providers.stream()
                .map(provider -> provider.findPrice(normalizedTicker))
                .flatMap(java.util.Optional::stream)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No market data provider returned a price"));
    }

    @Cacheable(value = "fxRates", key = "#fromCurrency.toUpperCase() + '/' + #toCurrency.toUpperCase()")
    public BigDecimal getFxRate(String fromCurrency, String toCurrency) {
        String from = fromCurrency.toUpperCase();
        String to = toCurrency.toUpperCase();
        if (from.equals(to)) {
            return BigDecimal.ONE;
        }

        return providers.stream()
                .map(provider -> provider.findFxRate(from, to))
                .flatMap(java.util.Optional::stream)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No market data provider returned an FX rate"));
    }
}
