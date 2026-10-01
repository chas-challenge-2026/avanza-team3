package se.comerit.avanza.risk;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskResultTest {

    @Test
    void structureShouldMatchNativeRiskResultLayout() {
        ExposedRiskResult result = new ExposedRiskResult();

        assertEquals(List.of("volatility", "sharpe_ratio", "max_drawdown"), result.fieldOrder());
        assertEquals(24, result.size());
        assertEquals(0, result.offsetOf("volatility"));
        assertEquals(8, result.offsetOf("sharpe_ratio"));
        assertEquals(16, result.offsetOf("max_drawdown"));
    }

    private static class ExposedRiskResult extends RiskResult {
        private List<String> fieldOrder() {
            return getFieldOrder();
        }

        private int offsetOf(String fieldName) {
            return fieldOffset(fieldName);
        }
    }
}
