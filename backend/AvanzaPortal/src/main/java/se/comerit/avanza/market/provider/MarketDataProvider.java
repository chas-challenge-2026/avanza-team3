package se.comerit.avanza.market.provider;

import java.math.BigDecimal;
import java.util.Optional;

public interface MarketDataProvider {

    Optional<BigDecimal> findPrice(String ticker);

    Optional<BigDecimal> findFxRate(String fromCurrency, String toCurrency);
}
