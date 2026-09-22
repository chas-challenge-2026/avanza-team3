package se.comerit.avanza.risk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RiskServiceTest {

    @Test
    void computeShouldReturnMetricsFromNativeResult() {
        RiskLibrary library = (values, length, riskFreeRate, periodsPerYear, out) -> {
            assertEquals(3, length);
            assertEquals(0.02, riskFreeRate);
            assertEquals(252, periodsPerYear);
            out.volatility = 0.12;
            out.sharpe_ratio = 1.5;
            out.max_drawdown = 0.08;
            return RiskLibrary.RISK_OK;
        };
        RiskService service = new RiskService(library);

        RiskMetrics metrics = service.compute(new double[]{100.0, 101.0, 99.0}, 0.02, 252);

        assertEquals(0.12, metrics.volatility());
        assertEquals(1.5, metrics.sharpeRatio());
        assertEquals(0.08, metrics.maxDrawdown());
    }

    @Test
    void computeShouldThrowWhenNativeReturnsError() {
        RiskService service = new RiskService((values, length, riskFreeRate, periodsPerYear, out) ->
                RiskLibrary.RISK_ERROR_TOO_SHORT);

        RiskNativeException exception = assertThrows(
                RiskNativeException.class,
                () -> service.compute(new double[]{100.0, 101.0}, 0.02, 252)
        );

        assertEquals(RiskLibrary.RISK_ERROR_TOO_SHORT, exception.getStatus());
    }
}
