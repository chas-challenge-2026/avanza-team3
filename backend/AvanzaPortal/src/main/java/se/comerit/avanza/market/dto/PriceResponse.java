package se.comerit.avanza.market.dto;

import java.math.BigDecimal;

public record PriceResponse(
        String ticker,
        BigDecimal price
) {
}
