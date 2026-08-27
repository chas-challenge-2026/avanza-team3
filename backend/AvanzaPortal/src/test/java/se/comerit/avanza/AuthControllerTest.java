package se.comerit.avanza;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.comerit.avanza.auth.controller.AuthController;
import se.comerit.avanza.auth.service.AuthService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void shouldPreventSqlInjectionOnLogin() throws Exception {
        String sqlInjectionJsonPayload = """
                    "username": "' OR '1'='1",
                    "password": "whatever"
                """;

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sqlInjectionJsonPayload))
                .andExpect(status().is4xxClientError());

    }
}
