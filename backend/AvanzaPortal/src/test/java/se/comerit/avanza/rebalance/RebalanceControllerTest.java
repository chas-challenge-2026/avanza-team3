package se.comerit.avanza.rebalance;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.comerit.avanza.accountallocation.model.AssetCategory;
import se.comerit.avanza.rebalance.controller.RebalanceController;
import se.comerit.avanza.rebalance.dto.RebalancePreviewRequest;
import se.comerit.avanza.rebalance.dto.RebalancePreviewResponse;
import se.comerit.avanza.rebalance.dto.RebalanceProposedHoldingResponse;
import se.comerit.avanza.rebalance.service.RebalanceService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RebalanceControllerTest {
    @Mock private RebalanceService service;
    @InjectMocks private RebalanceController controller;
    private MockMvc mvc;

    @BeforeEach
    void setup() { mvc = MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    void previewSerializesFullProposedHoldingRowsForFrontend() throws Exception {
        var position = new RebalanceProposedHoldingResponse(
                22, "FND-SE", "Demo Sverige Indexfond", AssetCategory.FUND,
                BigDecimal.ZERO, new BigDecimal("2.5000"), new BigDecimal("125.00"),
                new BigDecimal("0.00"), new BigDecimal("312.50"));
        when(service.preview(eq(11), eq(7), any(RebalancePreviewRequest.class))).thenReturn(
                new RebalancePreviewResponse(11, "signed-proposal", new BigDecimal("1000.00"),
                        BigDecimal.ZERO, BigDecimal.ZERO, true,
                        List.of(), List.of(), List.of(), List.of(position), List.of()));
        mvc.perform(post("/api/accounts/11/rebalance/preview")
                        .principal(authenticationForUser(7))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"allowedInstrumentIds\":[22]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(11))
                .andExpect(jsonPath("$.proposalToken").value("signed-proposal"))
                .andExpect(jsonPath("$.proposedHoldings[0].instrumentId").value(22))
                .andExpect(jsonPath("$.proposedHoldings[0].category").value("FUND"))
                .andExpect(jsonPath("$.proposedHoldings[0].currentQuantity").value(0))
                .andExpect(jsonPath("$.proposedHoldings[0].proposedQuantity").value(2.5))
                .andExpect(jsonPath("$.proposedHoldings[0].proposedValueSek").value(312.5));
    }

    private UsernamePasswordAuthenticationToken authenticationForUser(int userId) {
        var auth = new UsernamePasswordAuthenticationToken("test@example.com", null, Collections.emptyList());
        auth.setDetails(userId);
        return auth;
    }
}
