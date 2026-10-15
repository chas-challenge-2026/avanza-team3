package se.comerit.avanza.pricehistory.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.pricehistory.dto.HistoricalPricePoint;

import java.time.LocalDate;
import java.util.List;

@Component
@ConditionalOnProperty(name = "market.history.provider", havingValue = "stub", matchIfMissing = true)
public class StubHistoricalPriceProvider implements HistoricalPriceProvider {
    @Override
    public String source() {
        return "STUB";
    }

    @Override
    public List<HistoricalPricePoint> fetchPrices(Instrument instrument, LocalDate from, LocalDate to) {
        // Add explicit fixtures or a CSV reader here if development data is needed.
        return List.of();
    }
}
