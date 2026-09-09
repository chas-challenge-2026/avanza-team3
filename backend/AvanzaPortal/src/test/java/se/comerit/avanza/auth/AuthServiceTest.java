package se.comerit.avanza.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import se.comerit.avanza.auth.model.User;
import se.comerit.avanza.auth.repository.UserRepository;
import se.comerit.avanza.auth.service.AuthService;
import se.comerit.avanza.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    JwtService jwtService;

    @Test
    void validCredentialsReturnJwtToken() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        User user = new User("Test User", "test@example.com", encoder.encode("password123"));

        user.setId(1);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken("test@example.com", 1))
                .thenReturn("test-jwt-token");

        AuthService authService = new AuthService(userRepository, jwtService, encoder);

        String result = authService.authenticateAndGenerateToken("test@example.com", "password123");

        assertEquals("test-jwt-token", result);
    }

    @Test
    void wrongPasswordReturnsNull() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        User user = new User("Test User", "test@example.com", encoder.encode("correct-password"));

        user.setId(1);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        AuthService authService = new AuthService(userRepository, jwtService, encoder);

        String result = authService.authenticateAndGenerateToken("test@example.com", "wrong-password");

        assertNull(result);

        verify(jwtService, never())
                .generateToken(anyString(), anyInt());

    }

    @Test
    void unknownEmailReturnsNull() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        AuthService authService = new AuthService(userRepository, jwtService, encoder);

        String result = authService.authenticateAndGenerateToken("unknown@example.com", "password123");

        assertNull(result);

        verify(jwtService, never())
                .generateToken(anyString(), anyInt());

    }
}
