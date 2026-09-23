package se.comerit.avanza.auth.controller;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.comerit.avanza.auth.dto.LoginRequest;
import se.comerit.avanza.auth.model.User;
import se.comerit.avanza.auth.repository.UserRepository;
import se.comerit.avanza.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Authentication",
        description = "Endpoints for login, logout and information about the authenticated user"
)
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Operation(
            summary = "Log in",
            description = "Authenticates a user and returns a JWT access token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = authService.authenticateAndGenerateToken(request.getEmail(), request.getPassword());

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Fel e-post eller lösenord."));
        }

        return ResponseEntity.ok(Map.of(
                "token", token,
                "tokenType", "Bearer"));
    }

    @Operation(
            summary = "Log out",
            description = "Returns a logout confirmation. The current stateless JWT implementation does not revoke the token server-side."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout confirmation returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Utloggning lyckades."));
    }

    @Operation(
            summary = "Get current user",
            description = "Returns basic information about the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current user retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @Parameter(hidden = true)
            Authentication authentication) {

        Integer userId = (Integer) authentication.getDetails();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Användaren hittades inte."));

        return ResponseEntity.ok(
                Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail()
                )
        );
    }
}
