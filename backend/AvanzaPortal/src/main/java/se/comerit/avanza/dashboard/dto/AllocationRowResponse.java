package se.comerit.avanza.dashboard.dto;

import java.math.BigDecimal;

public record AllocationRowResponse(
        String accountType,
        BigDecimal actual,
        BigDecimal target,
        BigDecimal drift,
        boolean overThreshold
) {
}
