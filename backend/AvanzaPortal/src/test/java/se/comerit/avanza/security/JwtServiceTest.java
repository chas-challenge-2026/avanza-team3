package se.comerit.avanza.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secretKeyString", "test-secret-key-with-at-least-32-characters");

        ReflectionTestUtils.setField(jwtService, "expirationTimeMs", 86_400_000L);
    }

    @Test
    void generatedTokenContainsEmailAndUserId() {
        String token = jwtService.generateToken("test@example.com", 1);

        assertEquals("test@example.com", jwtService.extractEmail(token));

        assertEquals(1, jwtService.extractUserId(token));

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void manipulatedTokenIsInvalid() {
        String token = jwtService.generateToken("test@example.com", 1);

        String manipulatedToken = token + "changed";

        assertFalse(jwtService.isTokenValid(manipulatedToken));
    }

    @Test 
    void expiredTokenIsInvalid() {
        ReflectionTestUtils.setField(jwtService, "expirationTimeMs", -1_000L);

        String token = jwtService.generateToken("test@example.com", 1);

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void malformedTokenIsInvalid() {
        assertFalse(jwtService.isTokenValid("not-a-jwt-token"));
    }

}
