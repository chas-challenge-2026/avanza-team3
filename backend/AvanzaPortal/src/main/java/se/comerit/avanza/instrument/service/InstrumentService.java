package se.comerit.avanza.instrument.service;

import org.springframework.stereotype.Service;
import se.comerit.avanza.instrument.repository.InstrumentRepository;

@Service
public class InstrumentService {

    private InstrumentRepository instrumentRepository;

    public InstrumentService(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

}
