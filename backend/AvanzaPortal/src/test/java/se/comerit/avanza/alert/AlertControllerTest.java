package se.comerit.avanza.alert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.comerit.avanza.alert.controller.AlertController;
import se.comerit.avanza.alert.dto.AlertResponse;
import se.comerit.avanza.alert.service.AlertService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AlertControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AlertService alertService;

    @InjectMocks
    private AlertController alertController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(alertController).build();
    }

    @Test
    void getAlertsShouldReturnUnauthorizedWithoutSessionUser() throws Exception {
        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(alertService);
    }

    @Test
    void getAlertsShouldReturnAlertResponseForSessionUser() throws Exception {
        MockHttpSession session = sessionForUser(7);
        AlertResponse response = new AlertResponse(
                42, "DRIFT", "Rebalance", false,
                LocalDateTime.of(2026, 9, 1, 10, 0)
        );
        when(alertService.getAlertsByUserId(7)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/alerts").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(42))
                .andExpect(jsonPath("$[0].alertType").value("DRIFT"))
                .andExpect(jsonPath("$[0].dismissed").value(false));

        verify(alertService).getAlertsByUserId(7);
    }

    @Test
    void dismissAlertShouldReturnNoContentAndPassUserIdForIdorCheck() throws Exception {
        MockHttpSession session = sessionForUser(7);

        mockMvc.perform(patch("/api/alerts/42/dismiss").session(session))
                .andExpect(status().isNoContent());

        verify(alertService).dismissAlert(42, 7);
    }

    @Test
    void getLiveAlertsShouldUseSessionUser() throws Exception {
        MockHttpSession session = sessionForUser(7);
        when(alertService.getLiveAlertsByUserId(7)).thenReturn(List.of());

        mockMvc.perform(get("/api/alerts/live").session(session))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(alertService).getLiveAlertsByUserId(7);
    }

    private MockHttpSession sessionForUser(Integer userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userId);
        return session;
    }
}
