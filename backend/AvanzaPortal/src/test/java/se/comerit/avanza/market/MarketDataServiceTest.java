package se.comerit.avanza.market;

import org.junit.jupiter.api.Test;
import se.comerit.avanza.market.provider.FallbackMarketDataProvider;
import se.comerit.avanza.market.provider.MarketDataProvider;
import se.comerit.avanza.market.service.MarketDataService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarketDataServiceTest {

    @Test
    void getPriceShouldUseFirstProviderThatReturnsPrice() {
        MarketDataProvider emptyProvider = new EmptyMarketDataProvider();
        MarketDataProvider apiProvider = new StubMarketDataProvider(new BigDecimal("123.45"), Optional.empty());
        MarketDataService service = new MarketDataService(List.of(emptyProvider, apiProvider, new FallbackMarketDataProvider()));

        assertEquals(new BigDecimal("123.45"), service.getPrice("AAPL"));
    }

    @Test
    void getFxRateShouldFallBackWhenNoApiProviderHasRate() {
        MarketDataService service = new MarketDataService(List.of(new EmptyMarketDataProvider(), new FallbackMarketDataProvider()));

        assertEquals(new BigDecimal("10.45"), service.getFxRate("USD", "SEK"));
    }

    private static class EmptyMarketDataProvider implements MarketDataProvider {

        @Override
        public Optional<BigDecimal> findPrice(String ticker) {
            return Optional.empty();
        }

        @Override
        public Optional<BigDecimal> findFxRate(String fromCurrency, String toCurrency) {
            return Optional.empty();
        }
    }

    private record StubMarketDataProvider(
            BigDecimal price,
            Optional<BigDecimal> fxRate
    ) implements MarketDataProvider {

        @Override
        public Optional<BigDecimal> findPrice(String ticker) {
            return Optional.of(price);
        }

        @Override
        public Optional<BigDecimal> findFxRate(String fromCurrency, String toCurrency) {
            return fxRate;
        }
    }
}
