package se.comerit.avanza.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Converted amount between two currencies")
public record FxConversionResponse(

        @Schema(description = "Original amount", example = "100.00")
        BigDecimal amount,

        @Schema(description = "Source currency", example = "USD")
        String fromCurrency,

        @Schema(description = "Target currency", example = "SEK")
        String toCurrency,

        @Schema(description = "Units of target currency per one unit of source currency", example = "10.45")
        BigDecimal rate,

        @Schema(description = "Converted amount in target currency", example = "1045.00")
        BigDecimal convertedAmount
) {
}
