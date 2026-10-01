package se.comerit.avanza.risk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RiskServiceTest {

    @Test
    void computeShouldForwardInputsAndReturnMetricsFromNativeResult() {
        double[] input = {100.0, 101.0, 99.0};
        CapturingRiskLibrary library = new CapturingRiskLibrary(RiskLibrary.RISK_OK);
        library.volatility = 0.12;
        library.sharpeRatio = 1.5;
        library.maxDrawdown = 0.08;
        RiskService service = new RiskService(library);

        RiskMetrics metrics = service.compute(input, 0.02, 252);

        assertEquals(1, library.calls);
        assertSame(input, library.values);
        assertEquals(input.length, library.length);
        assertEquals(0.02, library.riskFreeRate);
        assertEquals(252, library.periodsPerYear);
        assertNotNull(library.out);
        assertEquals(0.12, metrics.volatility());
        assertEquals(1.5, metrics.sharpeRatio());
        assertEquals(0.08, metrics.maxDrawdown());
    }

    @Test
    void computeShouldRejectNullValuesBeforeCallingNativeLibrary() {
        CapturingRiskLibrary library = new CapturingRiskLibrary(RiskLibrary.RISK_OK);
        RiskService service = new RiskService(library);

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> service.compute(null, 0.02, 252)
        );

        assertEquals("values", exception.getMessage());
        assertEquals(0, library.calls);
    }

    @ParameterizedTest
    @MethodSource("nativeErrors")
    void computeShouldThrowMappedExceptionWhenNativeReturnsError(NativeError nativeError) {
        RiskService service = new RiskService(new CapturingRiskLibrary(nativeError.status()));

        RiskNativeException exception = assertThrows(
                RiskNativeException.class,
                () -> service.compute(new double[]{100.0, 101.0, 99.0}, 0.02, 252)
        );

        assertEquals(nativeError.status(), exception.getStatus());
        assertEquals(
                "risk_compute failed with status " + nativeError.status() + ": " + nativeError.message(),
                exception.getMessage()
        );
    }

    static Stream<NativeError> nativeErrors() {
        return Stream.of(
                new NativeError(RiskLibrary.RISK_ERROR_NULL, "values or output result was null"),
                new NativeError(RiskLibrary.RISK_ERROR_TOO_SHORT, "at least 3 values are required"),
                new NativeError(RiskLibrary.RISK_ERROR_BAD_RATE, "risk-free rate must be finite and greater than -1.0"),
                new NativeError(RiskLibrary.RISK_ERROR_BAD_PERIODS, "periods per year must be between 1 and 365"),
                new NativeError(RiskLibrary.RISK_ERROR_INVALID_VALUE, "values must be finite and greater than 0.0"),
                new NativeError(999, "unknown native risk error")
        );
    }

    private record NativeError(int status, String message) {
    }

    private static class CapturingRiskLibrary implements RiskLibrary {
        private final int status;
        private double volatility;
        private double sharpeRatio;
        private double maxDrawdown;
        private double[] values;
        private int length;
        private double riskFreeRate;
        private int periodsPerYear;
        private RiskResult out;
        private int calls;

        private CapturingRiskLibrary(int status) {
            this.status = status;
        }

        @Override
        public int risk_compute(double[] values, int length, double riskFreeRate, int periodsPerYear, RiskResult out) {
            calls++;
            this.values = values;
            this.length = length;
            this.riskFreeRate = riskFreeRate;
            this.periodsPerYear = periodsPerYear;
            this.out = out;

            out.volatility = volatility;
            out.sharpe_ratio = sharpeRatio;
            out.max_drawdown = maxDrawdown;
            return status;
        }
    }
}
