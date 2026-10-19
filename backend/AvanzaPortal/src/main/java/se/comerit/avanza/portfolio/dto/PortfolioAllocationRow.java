package se.comerit.avanza.portfolio.dto;

import java.math.BigDecimal;

public record PortfolioAllocationRow(
        String accountType,
        BigDecimal actualPercentage,
        BigDecimal targetPercentage,
        BigDecimal driftPercentagePoints,
        boolean overThreshold
) {
}
