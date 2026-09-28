package se.comerit.avanza.risk;

import com.sun.jna.Library;

public interface RiskLibrary extends Library {
    int RISK_OK = 0;
    int RISK_ERROR_NULL = 1;
    int RISK_ERROR_TOO_SHORT = 2;
    int RISK_ERROR_BAD_RATE = 3;
    int RISK_ERROR_BAD_PERIODS = 4;
    int RISK_ERROR_INVALID_VALUE = 5;

    int risk_compute(
            double[] values,
            int length,
            double riskFreeRate,
            int periodsPerYear,
            RiskResult out
    );
}
