package se.comerit.avanza.risk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import se.comerit.avanza.nativebridge.NativeLibraryLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledOnOs(OS.WINDOWS)
class RiskNativeIntegrationTest {

    @Test
    void riskDllShouldLoadAndComputeMetricsThroughJna() {
        RiskLibrary riskLibrary = NativeLibraryLoader.load("risk", RiskLibrary.class);
        RiskResult result = new RiskResult();

        int status = riskLibrary.risk_compute(
                new double[]{100.0, 102.0, 101.0, 104.0},
                4,
                0.02,
                252,
                result
        );
        result.read();

        assertEquals(RiskLibrary.RISK_OK, status);
        assertTrue(Double.isFinite(result.volatility));
        assertTrue(Double.isFinite(result.sharpe_ratio));
        assertTrue(Double.isFinite(result.max_drawdown));
        assertTrue(result.volatility > 0.0);
        assertTrue(result.max_drawdown >= 0.0);
    }

    @Test
    void riskDllShouldReturnNativeErrorCodesThroughJna() {
        RiskLibrary riskLibrary = NativeLibraryLoader.load("risk", RiskLibrary.class);
        RiskResult result = new RiskResult();

        int status = riskLibrary.risk_compute(
                new double[]{100.0, 102.0},
                2,
                0.02,
                252,
                result
        );

        assertEquals(RiskLibrary.RISK_ERROR_TOO_SHORT, status);
    }
}
