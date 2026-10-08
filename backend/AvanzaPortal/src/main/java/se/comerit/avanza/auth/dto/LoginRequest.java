package se.comerit.avanza.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Credentials used to authenticate a user")
public class LoginRequest {

    @Schema(description = "User email address", example = "anna@example.com")
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @Schema(description = "User password", example = "password123")
    @NotBlank
    @Size(max = 100)
    private String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
