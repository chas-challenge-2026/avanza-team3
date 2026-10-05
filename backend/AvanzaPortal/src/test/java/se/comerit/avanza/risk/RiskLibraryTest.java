package se.comerit.avanza.risk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskLibraryTest {

    @Test
    void constantsShouldMatchNativeRiskHeader() {
        assertEquals(0, RiskLibrary.RISK_OK);
        assertEquals(1, RiskLibrary.RISK_ERROR_NULL);
        assertEquals(2, RiskLibrary.RISK_ERROR_TOO_SHORT);
        assertEquals(3, RiskLibrary.RISK_ERROR_BAD_RATE);
        assertEquals(4, RiskLibrary.RISK_ERROR_BAD_PERIODS);
        assertEquals(5, RiskLibrary.RISK_ERROR_INVALID_VALUE);
    }
}
