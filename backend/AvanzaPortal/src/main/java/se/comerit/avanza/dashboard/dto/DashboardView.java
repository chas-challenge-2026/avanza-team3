package se.comerit.avanza.dashboard.dto;

import java.util.List;

public record DashboardView(
        List<AccountSummaryResponse> accounts,
        HoldingPageResponse holdings,
        List<AllocationRowResponse> allocationRows,
        double totalPortfolioValue,
        List<RecentAlertResponse> recentAlerts,
        boolean anyDrift,
        double usdToSek
) {
}
