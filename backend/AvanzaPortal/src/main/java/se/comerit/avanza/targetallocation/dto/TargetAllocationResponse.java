package se.comerit.avanza.targetallocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Target allocation for an account type")
public record TargetAllocationResponse(

        @Schema(description = "Target allocation ID", example = "21")
        Integer id,

        @Schema(description = "Account type the target applies to", example = "ISK")
        String accountType,

        @Schema(description = "Target percentage for the account type", example = "60.00")
        BigDecimal targetPercentage
) {
}
