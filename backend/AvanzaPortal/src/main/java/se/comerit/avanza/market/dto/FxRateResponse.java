package se.comerit.avanza.market.dto;

import java.math.BigDecimal;

public record FxRateResponse(
        String fromCurrency,
        String toCurrency,
        BigDecimal rate
) {
}
