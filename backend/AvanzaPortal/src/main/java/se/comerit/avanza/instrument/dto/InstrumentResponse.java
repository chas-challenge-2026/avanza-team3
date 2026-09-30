package se.comerit.avanza.instrument.dto;

import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;

public record InstrumentResponse(
        Integer id,
        String ticker,
        String name,
        InstrumentType instrumentType,
        Sector sector,
        String currency
) {
}