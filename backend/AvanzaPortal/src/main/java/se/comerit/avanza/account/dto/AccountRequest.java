package se.comerit.avanza.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Data for creating or replacing an account. User ownership is taken from the JWT, not the body.")
public record AccountRequest(
        @Schema(description = "Account type, e.g. ISK, KF or Depa", example = "ISK")
        @NotBlank @Size(max = 10)
        String accountType,

        @Schema(description = "Account name", example = "Mitt långsiktiga ISK")
        @NotBlank @Size(max = 100)
        String accountName,

        @Schema(description = "ISO 4217 currency code. Defaults to SEK on creation when omitted", example = "SEK")
        @Pattern(regexp = "[A-Za-z]{3}", message = "Currency must be a three-letter ISO code")
        String currency
) {
}
