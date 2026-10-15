package se.comerit.avanza.pricehistory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.comerit.avanza.exception.BusinessRuleException;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.instrument.service.InstrumentService;
import se.comerit.avanza.pricehistory.dto.HistoricalPricePoint;
import se.comerit.avanza.pricehistory.dto.PriceHistoryResponse;
import se.comerit.avanza.pricehistory.model.InstrumentPrice;
import se.comerit.avanza.pricehistory.provider.HistoricalPriceProvider;
import se.comerit.avanza.pricehistory.provider.HistoricalPriceProviderException;
import se.comerit.avanza.pricehistory.repository.InstrumentPriceRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class HistoricalPriceService {
    private final InstrumentService instrumentService;
    private final InstrumentPriceRepository repository;
    private final HistoricalPriceProvider provider;

    public HistoricalPriceService(InstrumentService instrumentService,
                                  InstrumentPriceRepository repository,
                                  HistoricalPriceProvider provider) {
        this.instrumentService = instrumentService;
        this.repository = repository;
        this.provider = provider;
    }

    @Transactional(readOnly = true)
    public PriceHistoryResponse getHistory(Integer instrumentId, LocalDate from, LocalDate to) {
        validateRange(from, to);
        Instrument instrument = instrumentService.getById(instrumentId);
        return response(instrument, from, to);
    }

    /** Explicit import; reading history never makes an external API call. */
    @Transactional
    public PriceHistoryResponse importHistory(Integer instrumentId, LocalDate from, LocalDate to) {
        validateRange(from, to);
        Instrument instrument = instrumentService.getById(instrumentId);
        List<HistoricalPricePoint> points = provider.fetchPrices(instrument, from, to);
        validatePoints(points, instrument, from, to);

        Map<LocalDate, InstrumentPrice> existing = new HashMap<>();
        repository.findByInstrumentIdAndDateBetweenOrderByDateAsc(instrumentId, from, to)
                .forEach(price -> existing.put(price.getDate(), price));

        List<InstrumentPrice> prices = points.stream().map(point -> {
            InstrumentPrice price = existing.get(point.date());
            if (price == null) {
                return new InstrumentPrice(instrument, point, provider.source());
            }
            price.update(point, provider.source());
            return price;
        }).toList();
        repository.saveAll(prices);
        return response(instrument, from, to);
    }

    private PriceHistoryResponse response(Instrument instrument, LocalDate from, LocalDate to) {
        return new PriceHistoryResponse(instrument.getId(), instrument.getTicker(), provider.source(),
                repository.findByInstrumentIdAndDateBetweenOrderByDateAsc(instrument.getId(), from, to)
                        .stream().map(InstrumentPrice::toPoint).toList());
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new BusinessRuleException("from must be on or before to");
        }
    }

    private void validatePoints(List<HistoricalPricePoint> points, Instrument instrument,
                                LocalDate from, LocalDate to) {
        if (points == null) {
            throw new HistoricalPriceProviderException("Provider returned null history");
        }
        Set<LocalDate> dates = new HashSet<>();
        for (HistoricalPricePoint point : points) {
            if (point == null || point.date() == null || point.date().isBefore(from)
                    || point.date().isAfter(to) || !dates.add(point.date())
                    || !validPrice(point.close())
                    || (point.adjustedClose() != null && !validPrice(point.adjustedClose()))
                    || !instrument.getCurrency().equals(point.currency())) {
                throw new HistoricalPriceProviderException("Provider returned invalid daily prices");
            }
        }
    }

    private boolean validPrice(BigDecimal price) {
        return price != null && price.signum() > 0 && price.scale() <= 8
                && price.precision() - price.scale() <= 16;
    }
}
