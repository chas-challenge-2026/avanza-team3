package se.comerit.avanza.risk;

import org.junit.jupiter.api.Test;
import se.comerit.avanza.portfolio.service.PortfolioValuationService;
import se.comerit.avanza.risk.dto.PortfolioRiskResponse;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PortfolioRiskServiceTest {

    @Test
    void getPortfolioRiskShouldUseSharedCurrentPortfolioValueAndNativeRiskCalculation() {
        PortfolioValuationService portfolioValuationService = mock(PortfolioValuationService.class);
        when(portfolioValuationService.getPortfolioValueSek(7))
                .thenReturn(new BigDecimal("10000.00"));

        RiskService riskService = new RiskService(new FixedRiskLibrary());
        Clock clock = Clock.fixed(Instant.parse("2026-10-12T10:00:00Z"), ZoneId.of("UTC"));
        PortfolioRiskService service = new PortfolioRiskService(
                portfolioValuationService,
                riskService,
                clock
        );

        PortfolioRiskResponse response = service.getPortfolioRisk(7);

        assertEquals("SEK", response.baseCurrency());
        assertEquals("MOCKED", response.historySource());
        assertEquals(3, response.history().size());
        assertEquals("2026-10-10", response.history().get(0).date().toString());
        assertEquals(new BigDecimal("9850.00"), response.history().get(0).portfolioValue());
        assertEquals(new BigDecimal("10060.00"), response.history().get(1).portfolioValue());
        assertEquals(new BigDecimal("10000.00"), response.history().get(2).portfolioValue());
        assertEquals(0.12, response.current().volatility());
        assertEquals(1.5, response.current().sharpeRatio());
        assertEquals(0.08, response.current().maxDrawdown());

        verify(portfolioValuationService).getPortfolioValueSek(7);
    }

    private static class FixedRiskLibrary implements RiskLibrary {

        @Override
        public int risk_compute(
                double[] values,
                int length,
                double riskFreeRate,
                int periodsPerYear,
                RiskResult out) {

            assertEquals(3, length);
            assertEquals(9850.00, values[0]);
            assertEquals(10060.00, values[1]);
            assertEquals(10000.00, values[2]);
            assertEquals(0.02, riskFreeRate);
            assertEquals(252, periodsPerYear);

            out.volatility = 0.12;
            out.sharpe_ratio = 1.5;
            out.max_drawdown = 0.08;
            return RISK_OK;
        }
    }
}
