package se.comerit.avanza.risk.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Historical portfolio risk metrics for one date")
public record RiskHistoryPointResponse(

        @Schema(description = "Date for the risk snapshot", example = "2026-10-12")
        LocalDate date,

        @Schema(description = "Mocked or sourced portfolio value in the base currency", example = "125000.00")
        BigDecimal portfolioValue,

        @Schema(description = "Risk metrics for this date")
        RiskMetricResponse metrics
) {
}
