package se.comerit.avanza.holding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.comerit.avanza.holding.controller.HoldingController;
import se.comerit.avanza.holding.service.HoldingService;
import se.comerit.avanza.instrument.model.InstrumentType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HoldingSummaryControllerTest {

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
    void summaryShouldReturnCompleteFilteredDatasetForAuthenticatedUser() throws Exception {
        when(holdingService.getHoldingsSummary(7, 11, InstrumentType.STOCK))
                .thenReturn(List.of(Map.of(
                        "id", 31,
                        "ticker", "ERIC-B",
                        "marketValue", new BigDecimal("742.00")
                )));

        mockMvc.perform(get("/api/holdings/summary")
                        .param("accountId", "11")
                        .param("instrumentType", "STOCK")
                        .principal(authenticationForUser(7)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(31))
                .andExpect(jsonPath("$[0].ticker").value("ERIC-B"))
                .andExpect(jsonPath("$[0].marketValue").value(742.00));

        verify(holdingService).getHoldingsSummary(7, 11, InstrumentType.STOCK);
    }

    private UsernamePasswordAuthenticationToken authenticationForUser(Integer userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "test@example.com",
                        null,
                        Collections.emptyList()
                );
        authentication.setDetails(userId);
        return authentication;
    }
}
