package se.comerit.avanza.pricehistory.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.pricehistory.dto.HistoricalPricePoint;

import java.time.LocalDate;
import java.util.List;

@Component
@ConditionalOnProperty(name = "market.history.provider", havingValue = "api")
public class ApiHistoricalPriceProvider implements HistoricalPriceProvider {
    @Override
    public String source() {
        // Replace with the actual supplier's stable name when integrating.
        return "API";
    }

    @Override
    public List<HistoricalPricePoint> fetchPrices(Instrument instrument, LocalDate from, LocalDate to) {
        // TODO: Inject an HTTP client with timeouts and externally configured credentials/base URL.
        // Map instrument to supplier symbol, request daily prices and map JSON to our DTO.
        // Translate transport, rate-limit and unknown-symbol errors to provider exceptions.
        throw new HistoricalPriceProviderException("Historical price API is not implemented yet");
    }
}
