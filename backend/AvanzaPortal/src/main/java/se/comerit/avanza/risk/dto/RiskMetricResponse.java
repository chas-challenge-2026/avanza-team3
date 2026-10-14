package se.comerit.avanza.risk.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Portfolio risk metrics")
public record RiskMetricResponse(

        @Schema(description = "Annualized volatility", example = "0.12")
        double volatility,

        @Schema(description = "Sharpe ratio", example = "1.45")
        double sharpeRatio,

        @Schema(description = "Maximum drawdown", example = "0.08")
        double maxDrawdown,

        @Schema(description = "Risk score from 0 to 100", example = "42")
        int riskScore
) {
}
