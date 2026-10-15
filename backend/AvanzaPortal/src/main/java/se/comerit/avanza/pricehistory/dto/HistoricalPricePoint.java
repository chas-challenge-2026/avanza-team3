package se.comerit.avanza.pricehistory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HistoricalPricePoint(
        LocalDate date,
        BigDecimal close,
        BigDecimal adjustedClose,
        String currency
) {}
