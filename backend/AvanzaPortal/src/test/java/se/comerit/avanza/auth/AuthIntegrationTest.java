package se.comerit.avanza.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.comerit.avanza.auth.dto.LoginRequest;
import se.comerit.avanza.auth.model.User;
import se.comerit.avanza.auth.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = new User("Anna", "anna@example.com", passwordEncoder.encode("password123"));
        userRepository.save(user);
    }

    @Test
    void validLoginReturnsAccessTokenCookie() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("anna@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inloggning lyckades."))
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().path("access_token", "/"));
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("anna@example.com");
        request.setPassword("wrong-password");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownEmailReturnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

    }

    @Test
    void logoutWithValidJwtCookieReturnsSuccessAndDeletesCookie() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("anna@example.com");
        request.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andReturn();

        Cookie accessTokenCookie = loginResult.getResponse().getCookie("access_token");

        mockMvc.perform(delete("/api/auth/logout")
                .with(csrf())
                .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Utloggning lyckades."))
                .andExpect(cookie().value("access_token", ""))
                .andExpect(cookie().maxAge("access_token", 0));
    }

}
