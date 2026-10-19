package se.comerit.avanza.risk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.comerit.avanza.portfolio.service.PortfolioValuationService;
import se.comerit.avanza.risk.dto.PortfolioRiskResponse;
import se.comerit.avanza.risk.dto.RiskHistoryPointResponse;
import se.comerit.avanza.risk.dto.RiskMetricResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class PortfolioRiskService {

    private static final String BASE_CURRENCY = PortfolioValuationService.BASE_CURRENCY;
    private static final String HISTORY_SOURCE = "MOCKED";
    private static final double RISK_FREE_RATE = 0.02;
    private static final int PERIODS_PER_YEAR = 252;

    private final PortfolioValuationService portfolioValuationService;
    private final RiskService riskService;
    private final Clock clock;

    @Autowired
    public PortfolioRiskService(
            PortfolioValuationService portfolioValuationService,
            RiskService riskService) {
        this(portfolioValuationService, riskService, Clock.systemDefaultZone());
    }

    PortfolioRiskService(
            PortfolioValuationService portfolioValuationService,
            RiskService riskService,
            Clock clock) {
        this.portfolioValuationService = portfolioValuationService;
        this.riskService = riskService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PortfolioRiskResponse getPortfolioRisk(Integer userId) {
        BigDecimal currentValue = portfolioValuationService.getPortfolioValueSek(userId);
        List<BigDecimal> historyValues = createMockHistoryValues(currentValue);
        double[] values = historyValues.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .toArray();

        RiskMetricResponse current = values[values.length - 1] > 0.0
                ? toResponse(riskService.compute(values, RISK_FREE_RATE, PERIODS_PER_YEAR))
                : new RiskMetricResponse(0.0, 0.0, 0.0, 0);

        List<RiskHistoryPointResponse> history = createHistory(current, historyValues);

        return new PortfolioRiskResponse(
                BASE_CURRENCY,
                HISTORY_SOURCE,
                current,
                history
        );
    }

    private List<BigDecimal> createMockHistoryValues(BigDecimal currentValue) {
        return List.of(
                currentValue.multiply(new BigDecimal("0.985")).setScale(2, RoundingMode.HALF_UP),
                currentValue.multiply(new BigDecimal("1.006")).setScale(2, RoundingMode.HALF_UP),
                currentValue.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private List<RiskHistoryPointResponse> createHistory(
            RiskMetricResponse current,
            List<BigDecimal> historyValues) {
        LocalDate today = LocalDate.now(clock);

        return List.of(
                new RiskHistoryPointResponse(
                        today.minusDays(2),
                        historyValues.get(0),
                        scaledMetrics(current, new BigDecimal("0.92"))
                ),
                new RiskHistoryPointResponse(
                        today.minusDays(1),
                        historyValues.get(1),
                        scaledMetrics(current, new BigDecimal("1.08"))
                ),
                new RiskHistoryPointResponse(
                        today,
                        historyValues.get(2),
                        current
                )
        );
    }

    private RiskMetricResponse toResponse(RiskMetrics metrics) {
        return new RiskMetricResponse(
                round(metrics.volatility()),
                round(metrics.sharpeRatio()),
                round(metrics.maxDrawdown()),
                riskScore(metrics)
        );
    }

    private RiskMetricResponse scaledMetrics(RiskMetricResponse metrics, BigDecimal factor) {
        return new RiskMetricResponse(
                round(metrics.volatility() * factor.doubleValue()),
                round(metrics.sharpeRatio() * factor.doubleValue()),
                round(metrics.maxDrawdown() * factor.doubleValue()),
                Math.max(0, Math.min(100, factor.multiply(BigDecimal.valueOf(metrics.riskScore()))
                        .setScale(0, RoundingMode.HALF_UP)
                        .intValue()))
        );
    }

    private int riskScore(RiskMetrics metrics) {
        double rawScore = metrics.volatility() * 220.0
                + metrics.maxDrawdown() * 180.0
                - metrics.sharpeRatio() * 8.0;
        return Math.max(0, Math.min(100, (int) Math.round(rawScore)));
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
