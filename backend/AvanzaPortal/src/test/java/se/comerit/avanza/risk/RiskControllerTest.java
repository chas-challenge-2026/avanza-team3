package se.comerit.avanza.risk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.comerit.avanza.risk.controller.RiskController;
import se.comerit.avanza.risk.dto.PortfolioRiskResponse;
import se.comerit.avanza.risk.dto.RiskHistoryPointResponse;
import se.comerit.avanza.risk.dto.RiskMetricResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RiskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PortfolioRiskService portfolioRiskService;

    @InjectMocks
    private RiskController riskController;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(riskController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getPortfolioRiskShouldReturnUnauthorizedWithoutAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/risk/portfolio"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(portfolioRiskService);
    }

    @Test
    void getPortfolioRiskShouldReturnRiskMetricsForAuthenticatedUser() throws Exception {
        PortfolioRiskResponse response = new PortfolioRiskResponse(
                "SEK",
                "MOCKED",
                new RiskMetricResponse(0.12, 1.45, 0.08, 42),
                List.of(
                        new RiskHistoryPointResponse(
                                LocalDate.of(2026, 10, 10),
                                new BigDecimal("98500.00"),
                                new RiskMetricResponse(0.11, 1.33, 0.07, 39)
                        ),
                        new RiskHistoryPointResponse(
                                LocalDate.of(2026, 10, 11),
                                new BigDecimal("100600.00"),
                                new RiskMetricResponse(0.13, 1.57, 0.09, 45)
                        ),
                        new RiskHistoryPointResponse(
                                LocalDate.of(2026, 10, 12),
                                new BigDecimal("100000.00"),
                                new RiskMetricResponse(0.12, 1.45, 0.08, 42)
                        )
                )
        );
        when(portfolioRiskService.getPortfolioRisk(7)).thenReturn(response);

        mockMvc.perform(get("/api/risk/portfolio").principal(authenticatedUser(7)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseCurrency").value("SEK"))
                .andExpect(jsonPath("$.historySource").value("MOCKED"))
                .andExpect(jsonPath("$.current.volatility").value(0.12))
                .andExpect(jsonPath("$.current.sharpeRatio").value(1.45))
                .andExpect(jsonPath("$.current.maxDrawdown").value(0.08))
                .andExpect(jsonPath("$.current.riskScore").value(42))
                .andExpect(jsonPath("$.history.length()").value(3))
                .andExpect(jsonPath("$.history[0].date").value("2026-10-10"));

        verify(portfolioRiskService).getPortfolioRisk(7);
    }

    private UsernamePasswordAuthenticationToken authenticatedUser(Integer userId) {
        return new UsernamePasswordAuthenticationToken(userId.toString(), null, List.of());
    }
}
