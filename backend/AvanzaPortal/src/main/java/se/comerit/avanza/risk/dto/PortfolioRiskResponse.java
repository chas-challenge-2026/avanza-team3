package se.comerit.avanza.risk.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Current and historical portfolio risk metrics")
public record PortfolioRiskResponse(

        @Schema(description = "Base currency used for portfolio values", example = "SEK")
        String baseCurrency,

        @Schema(description = "Source of the historical values", example = "MOCKED")
        String historySource,

        @Schema(description = "Current portfolio risk metrics")
        RiskMetricResponse current,

        @Schema(description = "Risk history, oldest date first")
        List<RiskHistoryPointResponse> history
) {
}
