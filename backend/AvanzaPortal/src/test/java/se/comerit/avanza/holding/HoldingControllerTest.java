package se.comerit.avanza.holding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.comerit.avanza.holding.controller.HoldingController;
import se.comerit.avanza.holding.service.HoldingService;


import java.util.List;

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
    void listHoldingsShouldReturnUnauthorizedWithoutSessionUser() throws Exception {
        mockMvc.perform(get("/api/holdings"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(holdingService);
    }

    @Test
    void addHoldingShouldReturnCreatedAndPassSessionUserToService() throws Exception {
        MockHttpSession session = sessionForUser(7);
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
                        .session(session)
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
        MockHttpSession session = sessionForUser(7);
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
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(holdingService);
    }

    @Test
    void deleteHoldingShouldReturnNoContentAndPassUserIdForOwnershipCheck() throws Exception {
        MockHttpSession session = sessionForUser(7);

        mockMvc.perform(delete("/api/holdings/31").session(session))
                .andExpect(status().isNoContent());

        verify(holdingService).deleteHolding(31, 7);
    }

    private MockHttpSession sessionForUser(Integer userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userId);
        return session;
    }

    @Test
    void listHoldingsShouldClampInvalidPaginationValues() throws Exception {
        MockHttpSession session = sessionForUser(7);
        when(holdingService.getHoldingsByUserId(7, 0, 100))
                .thenReturn(
                        new PageImpl<>(
                                List.of(),
                                PageRequest.of(0, 100),
                                0
                        )
                );

        mockMvc.perform(get("/api/holdings")
                        .param("page", "-3")
                        .param("size", "500")
                        .session(session))
                .andExpect(status().isOk());

        verify(holdingService).getHoldingsByUserId(7, 0, 100);
    }
}
