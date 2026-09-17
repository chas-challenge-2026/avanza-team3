package se.comerit.avanza.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Market price for an instrument")
public record PriceResponse(

        @Schema(description = "Instrument ticker", example = "AAPL")
        String ticker,

        @Schema(description = "Current instrument price", example = "187.32")
        BigDecimal price
) {
}
