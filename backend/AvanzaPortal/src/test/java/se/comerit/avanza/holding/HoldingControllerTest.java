package se.comerit.avanza.holding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.comerit.avanza.holding.controller.HoldingController;
import se.comerit.avanza.holding.service.HoldingService;


import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HoldingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HoldingService holdingService;

    @InjectMocks
    private HoldingController holdingController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(holdingController).build();
    }

    @Test
    void listHoldingsShouldUseAuthenticatedUser() throws Exception {
        when(holdingService.getHoldingsByUserId(7, 0, 20)).thenReturn(Page.empty());

        mockMvc.perform(get("/api/holdings")
                        .principal(authenticationForUser(7)))
                .andExpect(status().isOk());

        verify(holdingService).getHoldingsByUserId(7, 0, 20);
    }

    @Test
    void addHoldingShouldReturnCreatedAndPassSessionUserToService() throws Exception {
        String body = """
                {
                  "accountId": 11,
                  "ticker": "ERIC-B",
                  "instrumentName": "Ericsson B",
                  "quantity": 5,
                  "avgBuyPrice": 71.50,
                  "currency": "SEK"
                }
                """;

        mockMvc.perform(post("/api/holdings")
                        .principal(authenticationForUser(7))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(holdingService).addHolding(
                eq(7), eq(11), eq("ERIC-B"), eq("Ericsson B"),
                eq(new java.math.BigDecimal("5")),
                eq(new java.math.BigDecimal("71.50")),
                eq("SEK")
        );
    }

    @Test
    void addHoldingShouldRejectInvalidRequestBeforeCallingService() throws Exception {
        String body = """
                {
                  "accountId": 11,
                  "ticker": "",
                  "instrumentName": "Ericsson B",
                  "quantity": 0,
                  "avgBuyPrice": 71.50,
                  "currency": "SEK"
                }
                """;

        mockMvc.perform(post("/api/holdings")
                        .principal(authenticationForUser(7))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(holdingService);
    }

    @Test
    void deleteHoldingShouldReturnNoContentAndPassUserIdForOwnershipCheck() throws Exception {
        mockMvc.perform(delete("/api/holdings/31").principal(authenticationForUser(7)))

                .andExpect(status().isNoContent());

        verify(holdingService).deleteHolding(31, 7);
    }

    private MockHttpSession sessionForUser(Integer userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userId);
        return session;
    }

    private UsernamePasswordAuthenticationToken authenticationForUser(Integer userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "test@example.com",
                        null,
                        Collections.emptyList());
        authentication.setDetails(userId);
        return authentication;
    }
}
