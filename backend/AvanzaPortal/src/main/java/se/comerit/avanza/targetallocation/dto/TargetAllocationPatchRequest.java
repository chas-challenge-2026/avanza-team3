package se.comerit.avanza.targetallocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Fields that can be updated on a target allocation")
public record TargetAllocationPatchRequest(

        @Schema(description = "Account type the target applies to", example = "ISK")
        @Size(min = 1, max = 10)
        String accountType,

        @Schema(description = "Target percentage for the account type", example = "55.00")
        @DecimalMin(value = "0.00")
        @DecimalMax(value = "100.00")
        @Digits(integer = 3, fraction = 2)
        BigDecimal targetPercentage
) {
    @AssertTrue(message = "At least one field must be provided")
    public boolean isUpdatePresent() {
        return accountType != null || targetPercentage != null;
    }

    @AssertTrue(message = "Account type must not be blank")
    public boolean isAccountTypeValid() {
        return accountType == null || !accountType.isBlank();
    }
}
