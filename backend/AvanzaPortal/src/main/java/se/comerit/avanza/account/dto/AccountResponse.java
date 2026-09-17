package se.comerit.avanza.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Account information for the authenticated user")
public record AccountResponse(

        @Schema(description = "Account ID", example = "1")
        Integer id,

        @Schema(description = "Account type", example = "ISK")
        String accountType,

        @Schema(description = "Account display name", example = "Main ISK")
        String accountName,

        @Schema(description = "Account currency", example = "SEK")
        String currency
) {
}
