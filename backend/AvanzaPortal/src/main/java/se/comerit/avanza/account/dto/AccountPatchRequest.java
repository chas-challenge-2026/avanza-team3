package se.comerit.avanza.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Fields to update; omitted fields remain unchanged")
public record AccountPatchRequest(
        @Size(max = 10) String accountType,
        @Size(max = 100) String accountName,
        @Pattern(regexp = "[A-Za-z]{3}", message = "Currency must be a three-letter ISO code") String currency
) {
    @AssertTrue(message = "At least one field must be provided")
    public boolean isUpdatePresent() {
        return accountType != null || accountName != null || currency != null;
    }

    @AssertTrue(message = "Account type must not be blank")
    public boolean isAccountTypeValid() {
        return accountType == null || !accountType.isBlank();
    }

    @AssertTrue(message = "Account name must not be blank")
    public boolean isAccountNameValid() {
        return accountName == null || !accountName.isBlank();
    }
}
