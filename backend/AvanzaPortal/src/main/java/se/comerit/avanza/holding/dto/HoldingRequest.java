package se.comerit.avanza.holding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Data required to create a new holding")
public record HoldingRequest(

        @Schema(
                description = "ID of the account where the holding should be created.",
                example = "1"
        )
        @NotNull
        Integer accountId,

        @Schema(
                description = "ID of an existing instrument to attach to the holding.",
                example = "1"
        )
        @NotNull
        Integer instrumentId,

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
        BigDecimal avgBuyPrice

) {}
