package se.comerit.avanza.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Portfolio summary for one account")
public record AccountSummaryResponse(

        @Schema(description = "Account ID", example = "1")
        Integer id,

        @Schema(description = "Owner user ID", example = "7")
        Integer userId,

        @Schema(description = "Account type", example = "ISK")
        String accountType,

        @Schema(description = "Account display name", example = "Main ISK")
        String accountName,

        @Schema(description = "Account currency", example = "SEK")
        String currency,

        @Schema(description = "Total current value of the account in SEK", example = "125000.50")
        BigDecimal totalValueSek
) {
}
