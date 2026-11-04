package se.comerit.avanza.portfolio.dto;

import se.comerit.avanza.instrument.model.InstrumentType;

import java.math.BigDecimal;

public record AggregatedValuedHolding(
        Integer accountId,
        String accountType,
        InstrumentType instrumentType,
        BigDecimal marketValueSek
) implements PortfolioValuationEntry {
}
