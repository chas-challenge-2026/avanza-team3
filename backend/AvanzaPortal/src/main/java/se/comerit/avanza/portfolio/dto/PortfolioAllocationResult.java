package se.comerit.avanza.portfolio.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioAllocationResult(
        List<PortfolioAllocationRow> rows,
        BigDecimal totalPortfolioValueSek,
        boolean anyDrift
) {
}
