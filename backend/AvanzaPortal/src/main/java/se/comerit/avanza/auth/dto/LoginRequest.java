package se.comerit.avanza.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credentials used to authenticate a user")
public class LoginRequest {

    @Schema(description = "User email address", example = "anna@example.com")
    private String email;

    @Schema(description = "User password", example = "password123")
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
