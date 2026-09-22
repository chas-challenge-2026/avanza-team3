package se.comerit.avanza.risk;

public record RiskMetrics(
        double volatility,
        double sharpeRatio,
        double maxDrawdown
) {
}
