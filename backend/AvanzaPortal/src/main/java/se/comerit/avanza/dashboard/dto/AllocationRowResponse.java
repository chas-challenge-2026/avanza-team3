package se.comerit.avanza.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Actual allocation compared with the target for one account type")
public record AllocationRowResponse(

        @Schema(description = "Account type", example = "ISK")
        String accountType,

        @Schema(description = "Actual share of the portfolio in percent", example = "62.50")
        BigDecimal actual,

        @Schema(description = "Target share of the portfolio in percent", example = "60.00")
        BigDecimal target,

        @Schema(description = "Absolute difference between actual and target in percentage points", example = "2.50")
        BigDecimal drift,

        @Schema(description = "Whether the allocation exceeds the configured drift threshold", example = "false")
        boolean overThreshold
) {
}
