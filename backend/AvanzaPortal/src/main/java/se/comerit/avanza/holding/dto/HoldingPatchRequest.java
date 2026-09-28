package se.comerit.avanza.holding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Fields that can be updated on an existing holding")
public record HoldingPatchRequest(

        @Schema(
                description = "Instrument ticker. Omit the field if it should not be changed.",
                example = "AAPL"
        )
        @Size(max = 20)
        String ticker,

        @Schema(
                description = "Instrument name. Omit the field if it should not be changed.",
                example = "Apple Inc."
        )
        @Size(max = 100)
        String instrumentName,

        @Schema(
                description = "Number of units held. Must be greater than 0 if provided.",
                example = "10.0000"
        )
        @DecimalMin(value = "0.00001")
        @Digits(integer = 8, fraction = 4)
        BigDecimal quantity,

        @Schema(
                description = "Average purchase price per unit. Must be 0 or greater if provided.",
                example = "175.50"
        )
        @DecimalMin(value = "0")
        @Digits(integer = 10, fraction = 2)
        BigDecimal avgBuyPrice,

        @Schema(
                description = "Currency code for the holding.",
                example = "USD"
        )
        @Size(max = 3)
        String currency
) {}
