package se.comerit.avanza.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Aggregated portfolio dashboard response")
public record DashboardView(

        @Schema(description = "Account summaries")
        List<AccountSummaryResponse> accounts,

        @Schema(description = "Paginated holdings")
        HoldingPageResponse holdings,

        @Schema(description = "Actual allocation compared with target allocation")
        List<AllocationRowResponse> allocationRows,

        @Schema(description = "Total portfolio value in SEK", example = "245500.75")
        double totalPortfolioValue,

        @Schema(description = "Recent non-dismissed stored alerts")
        List<RecentAlertResponse> recentAlerts,

        @Schema(description = "Whether any allocation exceeds the configured drift threshold", example = "true")
        boolean anyDrift,

        @Schema(description = "USD to SEK exchange rate used by the dashboard", example = "10.45")
        double usdToSek
) {
}
