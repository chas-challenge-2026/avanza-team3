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

    @Test
    void convertShouldUseDirectFxRate() {
        MarketDataService service = new MarketDataService(List.of(new StubMarketDataProvider(
                BigDecimal.ZERO,
                Optional.of(new BigDecimal("10.00"))
        )));

        assertEquals(new BigDecimal("250.0000"), service.convert(new BigDecimal("25.00"), "USD", "SEK"));
    }

    @Test
    void convertShouldUseInverseFxRateWhenDirectRateIsMissing() {
        MarketDataService service = new MarketDataService(List.of(new InverseOnlyMarketDataProvider()));

        assertEquals(new BigDecimal("10.0000000000"), service.convert(new BigDecimal("100.00"), "SEK", "USD"));
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

    private static class InverseOnlyMarketDataProvider implements MarketDataProvider {

        @Override
        public Optional<BigDecimal> findPrice(String ticker) {
            return Optional.empty();
        }

        @Override
        public Optional<BigDecimal> findFxRate(String fromCurrency, String toCurrency) {
            if ("USD".equals(fromCurrency) && "SEK".equals(toCurrency)) {
                return Optional.of(new BigDecimal("10.00"));
            }

            return Optional.empty();
        }
    }
}
