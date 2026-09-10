package se.comerit.avanza.holding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.comerit.avanza.holding.controller.HoldingController;
import se.comerit.avanza.holding.dto.HoldingPatchRequest;
import se.comerit.avanza.holding.dto.HoldingResponse;
import se.comerit.avanza.holding.service.HoldingService;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        when(holdingService.getHoldingsByUserId(7, 0, 20)).thenReturn(Page.empty(PageRequest.of(0, 20)));
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
                eq(new BigDecimal("5")),
                eq(new BigDecimal("71.50")),
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

    private UsernamePasswordAuthenticationToken authenticationForUser(Integer userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "test@example.com",
                        null,
                        Collections.emptyList());
        authentication.setDetails(userId);
        return authentication;
    }

    @Test
    void getHoldingShouldReturnHoldingResponseForAuthenticatedUser() throws Exception {
        HoldingResponse response = holdingResponse(31, new BigDecimal("10"));
        when(holdingService.getHoldingById(31, 7)).thenReturn(response);

        mockMvc.perform(get("/api/holdings/31")
                        .principal(authenticationForUser(7)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(31))
                .andExpect(jsonPath("$.accountId").value(11))
                .andExpect(jsonPath("$.ticker").value("ERIC-B"))
                .andExpect(jsonPath("$.instrumentName").value("Ericsson B"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.avgBuyPrice").value(70.00))
                .andExpect(jsonPath("$.currency").value("SEK"))
                .andExpect(jsonPath("$.currentPrice").value(74.20))
                .andExpect(jsonPath("$.marketValueSek").value(742.00))
                .andExpect(jsonPath("$.pnlSek").value(42.00))
                .andExpect(jsonPath("$.pnlPct").value(6.00));

        verify(holdingService).getHoldingById(31, 7);
    }

    @Test
    void updateHoldingShouldReturnUpdatedHoldingAndPassAuthenticatedUserToService() throws Exception {
        HoldingResponse response = holdingResponse(31, new BigDecimal("15"));
        when(holdingService.updateHolding(eq(31), eq(7), any(HoldingPatchRequest.class)))
                .thenReturn(response);

        String body = """
                {
                  "quantity": 15
                }
                """;

        mockMvc.perform(patch("/api/holdings/31")
                        .principal(authenticationForUser(7))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(31))
                .andExpect(jsonPath("$.quantity").value(15))
                .andExpect(jsonPath("$.ticker").value("ERIC-B"));

        verify(holdingService).updateHolding(
                eq(31),
                eq(7),
                argThat(request ->
                        new BigDecimal("15").compareTo(request.quantity()) == 0
                                && request.ticker() == null
                                && request.instrumentName() == null
                                && request.avgBuyPrice() == null
                                && request.currency() == null
                )
        );
    }


    private HoldingResponse holdingResponse(Integer holdingId, BigDecimal quantity) {
        BigDecimal currentPrice = new BigDecimal("74.20");
        BigDecimal avgBuyPrice = new BigDecimal("70.00");
        BigDecimal marketValue = quantity.multiply(currentPrice).setScale(2);
        BigDecimal pnl = marketValue.subtract(quantity.multiply(avgBuyPrice)).setScale(2);
        BigDecimal pnlPct = new BigDecimal("6.00");

        return new HoldingResponse(
                holdingId,
                11,
                "ERIC-B",
                "Ericsson B",
                quantity,
                avgBuyPrice,
                "SEK",
                currentPrice,
                marketValue,
                pnl,
                pnlPct
        );
    }
}
