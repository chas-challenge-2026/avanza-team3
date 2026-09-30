package se.comerit.avanza.instrument.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;
import se.comerit.avanza.instrument.repository.InstrumentRepository;

@Service
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

    public InstrumentService(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    @Transactional
    public Instrument create(
            String ticker,
            String name,
            InstrumentType instrumentType,
            Sector sector,
            String currency
    ) {
        if (instrumentRepository.findByTickerIgnoreCase(ticker).isPresent()) {
            throw new IllegalArgumentException(
                    "Instrument with ticker " + ticker + " exists"
            );
        }

        Instrument instrument = new Instrument(
                ticker,
                name,
                instrumentType,
                sector,
                currency
        );

        return instrumentRepository.save(instrument);
    }

    @Transactional(readOnly = true)
    public Instrument getById(Integer id) {
        return instrumentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Instrument with id " + id + " not found"
                        )
                );
    }
}
