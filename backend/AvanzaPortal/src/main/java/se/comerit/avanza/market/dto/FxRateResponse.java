package se.comerit.avanza.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Foreign-exchange rate between two currencies")
public record FxRateResponse(

        @Schema(description = "Source currency", example = "USD")
        String fromCurrency,

        @Schema(description = "Target currency", example = "SEK")
        String toCurrency,

        @Schema(description = "Units of target currency per one unit of source currency", example = "10.45")
        BigDecimal rate
) {
}
