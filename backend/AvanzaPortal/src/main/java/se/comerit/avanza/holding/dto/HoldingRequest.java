package se.comerit.avanza.holding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Data required to create a new holding")
public record HoldingRequest(

        // Sen innan all backend slogs ihop. Ska plockas bort
        @Schema(
                description = "ID of the account where the holding should be created.",
                example = "1"
        )
        @NotNull
        Integer accountId,

        @Schema(
                description = "Instrument ticker.",
                example = "AAPL"
        )
        @NotBlank
        @Size(max = 20)
        String ticker,

        @Schema(
                description = "Instrument name.",
                example = "Apple Inc."
        )
        @NotBlank
        @Size(max = 100)
        String instrumentName,

        @Schema(
                description = "Number of units to add to the holding.",
                example = "10.0000"
        )
        @NotNull
        @DecimalMin(value = "0.00001")
        @Digits(integer = 8, fraction = 4)
        BigDecimal quantity,

        @Schema(
                description = "Average purchase price per unit.",
                example = "175.50"
        )
        @NotNull
        @DecimalMin(value = "0")
        @Digits(integer = 10, fraction = 2)
        BigDecimal avgBuyPrice,

        @Schema(
                description = "Currency code for the holding.",
                example = "USD"
        )
        @NotBlank
        @Size(max = 3)
        String currency

) {}
