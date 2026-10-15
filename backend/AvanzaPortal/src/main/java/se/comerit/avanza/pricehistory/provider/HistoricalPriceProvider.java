package se.comerit.avanza.pricehistory.provider;

import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.pricehistory.dto.HistoricalPricePoint;

import java.time.LocalDate;
import java.util.List;

public interface HistoricalPriceProvider {
    String source();
    List<HistoricalPricePoint> fetchPrices(Instrument instrument, LocalDate from, LocalDate to);
}
