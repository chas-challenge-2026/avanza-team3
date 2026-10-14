package se.comerit.avanza.portfolio.dto;

import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;

import java.math.BigDecimal;

public record ValuedHolding(
        Integer holdingId,
        Integer accountId,
        String accountType,
        String accountName,
        Integer instrumentId,
        String ticker,
        String instrumentName,
        InstrumentType instrumentType,
        Sector sector,
        String currency,
        BigDecimal quantity,
        BigDecimal avgBuyPrice,
        BigDecimal currentPrice,
        BigDecimal fxRateToSek,
        BigDecimal marketValueSek,
        BigDecimal costBasisSek,
        BigDecimal pnlSek,
        BigDecimal pnlPct
) {
}
