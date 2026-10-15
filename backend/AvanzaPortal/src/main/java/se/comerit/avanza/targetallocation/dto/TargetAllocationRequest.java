package se.comerit.avanza.targetallocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Data required to create a target allocation")
public record TargetAllocationRequest(

        @Schema(description = "Account type the target applies to", example = "ISK")
        @NotBlank
        @Size(max = 10)
        String accountType,

        @Schema(description = "Target percentage for the account type", example = "60.00")
        @NotNull
        @DecimalMin(value = "0.00")
        @DecimalMax(value = "100.00")
        @Digits(integer = 3, fraction = 2)
        BigDecimal targetPercentage
) {
}
