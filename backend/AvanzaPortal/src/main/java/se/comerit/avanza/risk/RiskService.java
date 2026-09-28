package se.comerit.avanza.risk;

import com.sun.jna.Native;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.function.Supplier;

@Service
public class RiskService {

    private final Supplier<RiskLibrary> riskLibrarySupplier;

    public RiskService() {
        this(() -> RiskLibraryHolder.INSTANCE);
    }

    RiskService(RiskLibrary riskLibrary) {
        this(() -> riskLibrary);
    }

    private RiskService(Supplier<RiskLibrary> riskLibrarySupplier) {
        this.riskLibrarySupplier = Objects.requireNonNull(riskLibrarySupplier, "riskLibrarySupplier");
    }

    public RiskMetrics compute(double[] values, double riskFreeRate, int periodsPerYear) {
        Objects.requireNonNull(values, "values");

        RiskResult result = new RiskResult();
        int status = riskLibrarySupplier.get().risk_compute(values, values.length, riskFreeRate, periodsPerYear, result);
        if (status != RiskLibrary.RISK_OK) {
            throw new RiskNativeException(status, statusMessage(status));
        }

        return new RiskMetrics(result.volatility, result.sharpe_ratio, result.max_drawdown);
    }

    private static String statusMessage(int status) {
        return switch (status) {
            case RiskLibrary.RISK_ERROR_NULL -> "values or output result was null";
            case RiskLibrary.RISK_ERROR_TOO_SHORT -> "at least 3 values are required";
            case RiskLibrary.RISK_ERROR_BAD_RATE -> "risk-free rate must be finite and greater than -1.0";
            case RiskLibrary.RISK_ERROR_BAD_PERIODS -> "periods per year must be between 1 and 365";
            case RiskLibrary.RISK_ERROR_INVALID_VALUE -> "values must be finite and greater than 0.0";
            default -> "unknown native risk error";
        };
    }

    private static class RiskLibraryHolder {
        private static final RiskLibrary INSTANCE = Native.load("risk", RiskLibrary.class);
    }
}
