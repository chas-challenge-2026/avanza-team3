package se.comerit.avanza.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import se.comerit.avanza.auth.dto.LoginRequest;
import se.comerit.avanza.auth.model.User;
import se.comerit.avanza.auth.repository.UserRepository;
import se.comerit.avanza.auth.service.AuthService;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

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

    @DeleteMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Utloggning lyckades."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication){
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
