package se.comerit.avanza.instrument.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.comerit.avanza.exception.ResourceConflictException;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.instrument.dto.InstrumentResponse;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;
import se.comerit.avanza.instrument.repository.InstrumentRepository;

import java.util.List;
import java.util.Locale;

@Service
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

    public InstrumentService(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    @Transactional
    public void create(
            String ticker,
            String name,
            InstrumentType instrumentType,
            Sector sector,
            String currency
    ) {
        String normalizedTicker = normalizeTicker(ticker);
        if (instrumentRepository.findByTickerIgnoreCase(normalizedTicker).isPresent()) {
            throw new ResourceConflictException(
                    "Instrument with ticker " + normalizedTicker + " already exists"
            );
        }

        Instrument instrument = new Instrument(
                normalizedTicker,
                name.trim(),
                instrumentType,
                sector,
                currency.trim().toUpperCase(Locale.ROOT)
        );

        instrumentRepository.save(instrument);
    }

    @Transactional(readOnly = true)
    public Instrument getById(Integer instrumentId) {
        return instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Instrument with id " + instrumentId + " not found"
                ));
    }

    @Transactional(readOnly = true)
    public List<InstrumentResponse> getAllInstruments() {
        return instrumentRepository.findAllByOrderByTickerAsc()
                .stream()
                .map(this::toInstrumentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InstrumentResponse getInstrumentById(Integer instrumentId) {
        return toInstrumentResponse(getById(instrumentId));
    }

    private String normalizeTicker(String ticker) {
        return ticker.trim().toUpperCase(Locale.ROOT);
    }

    private InstrumentResponse toInstrumentResponse(Instrument instrument) {
        return new InstrumentResponse(
                instrument.getId(),
                instrument.getTicker(),
                instrument.getName(),
                instrument.getInstrumentType(),
                instrument.getSector(),
                instrument.getCurrency()
        );
    }
}
