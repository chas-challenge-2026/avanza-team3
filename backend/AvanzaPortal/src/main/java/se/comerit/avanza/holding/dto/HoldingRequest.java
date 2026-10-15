package se.comerit.avanza.holding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Data required to create a new holding")
public record HoldingRequest(

        @Schema(
                description = "ID of the account where the holding should be created.",
                example = "1"
        )
        @NotNull
        @Positive
        Integer accountId,

        @Schema(
                description = "Ticker of an existing instrument. Instrument metadata is resolved in the backend.",
                example = "AAPL"
        )
        @NotBlank
        @Size(max = 20)
        String ticker,

        @Schema(
                description = "Frontend compatibility field. The backend does not use this as the source of truth.",
                example = "Apple Inc."
        )
        @Size(max = 100)
        String instrumentName,

        @Schema(
                description = "Number of units to add to the holding.",
                example = "10.0000"
        )
        @NotNull
        @DecimalMin(value = "0.0001")
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
                description = "Frontend compatibility field. Currency is read from the backend instrument catalog.",
                example = "USD"
        )
        @Size(max = 3)
        String currency

) {}
