package se.comerit.avanza.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.comerit.avanza.dashboard.controller.DashboardController;
import se.comerit.avanza.dashboard.dto.AccountSummaryResponse;
import se.comerit.avanza.dashboard.dto.AllocationRowResponse;
import se.comerit.avanza.dashboard.dto.DashboardView;
import se.comerit.avanza.dashboard.dto.HoldingPageResponse;
import se.comerit.avanza.dashboard.dto.HoldingSummaryResponse;
import se.comerit.avanza.dashboard.dto.RecentAlertResponse;
import se.comerit.avanza.dashboard.service.DashboardService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
    }

    @Test
    void dashboardShouldReturnUnauthorizedWithoutAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/portfolio"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(dashboardService);
    }

    @Test
    void dashboardShouldReturnPortfolioForAuthenticatedUser() throws Exception {
        DashboardView dashboard = new DashboardView(
                List.of(new AccountSummaryResponse(11, 7, "ISK", "Main ISK", "SEK", new BigDecimal("742.00"))),
                new HoldingPageResponse(
                        List.of(new HoldingSummaryResponse(
                                31,
                                11,
                                "ERIC-B",
                                "Ericsson B",
                                new BigDecimal("10"),
                                new BigDecimal("70.00"),
                                "SEK",
                                "ISK",
                                "Main ISK",
                                new BigDecimal("74.20"),
                                new BigDecimal("742.00"),
                                new BigDecimal("42.00"),
                                new BigDecimal("6.00"),
                                new BigDecimal("0.27"),
                                "SEK"
                        )),
                        2,
                        5,
                        11,
                        3,
                        false,
                        false
                ),
                List.of(new AllocationRowResponse("ISK", new BigDecimal("100.00"), new BigDecimal("60.00"), new BigDecimal("40.00"), true)),
                742.00,
                List.of(new RecentAlertResponse(41, "DRIFT", "Rebalance", LocalDateTime.of(2026, 9, 1, 10, 0))),
                true,
                10.45
        );
        when(dashboardService.getDashboardForUser(7, 2, 5)).thenReturn(dashboard);

        mockMvc.perform(get("/api/portfolio?page=2&size=5").principal(authenticatedUser(7)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts[0].accountName").value("Main ISK"))
                .andExpect(jsonPath("$.holdings.content[0].ticker").value("ERIC-B"))
                .andExpect(jsonPath("$.holdings.content[0].instrumentName").value("Ericsson B"))
                .andExpect(jsonPath("$.holdings.page").value(2))
                .andExpect(jsonPath("$.holdings.size").value(5))
                .andExpect(jsonPath("$.holdings.totalElements").value(11))
                .andExpect(jsonPath("$.allocationRows[0].accountType").value("ISK"))
                .andExpect(jsonPath("$.totalPortfolioValue").value(742.00))
                .andExpect(jsonPath("$.recentAlerts[0].alertType").value("DRIFT"))
                .andExpect(jsonPath("$.anyDrift").value(true))
                .andExpect(jsonPath("$.usdToSek").value(10.45));

        verify(dashboardService).getDashboardForUser(7, 2, 5);
    }

    @Test
    void dashboardShouldNormalizePaginationParameters() throws Exception {
        DashboardView dashboard = new DashboardView(
                List.of(),
                new HoldingPageResponse(List.of(), 0, 20, 0, 0, true, true),
                List.of(),
                0.00,
                List.of(),
                false,
                10.45
        );
        when(dashboardService.getDashboardForUser(7, 0, 20)).thenReturn(dashboard);

        mockMvc.perform(get("/api/portfolio?page=-1&size=0").principal(authenticatedUser(7)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdings.page").value(0))
                .andExpect(jsonPath("$.holdings.size").value(20));

        verify(dashboardService).getDashboardForUser(7, 0, 20);
    }

    private UsernamePasswordAuthenticationToken authenticatedUser(Integer userId) {
        return new UsernamePasswordAuthenticationToken(userId.toString(), null, List.of());
    }
}
