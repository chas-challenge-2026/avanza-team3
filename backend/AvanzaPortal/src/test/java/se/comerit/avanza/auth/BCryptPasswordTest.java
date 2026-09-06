package se.comerit.avanza.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BCryptPasswordTest {
    
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    @Test
    void correctPasswordMatchesHash() {
        String hash = encoder.encode("password123");

        assertTrue(encoder.matches("password123", hash));
    }

    @Test 
    void wrongPasswordDoesNotMatchHash() {
        String hash = encoder.encode("password123");

        assertFalse(encoder.matches("wrong-password", hash));
    }
}
