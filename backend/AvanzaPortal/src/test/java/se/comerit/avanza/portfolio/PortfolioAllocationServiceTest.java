package se.comerit.avanza.portfolio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;
import se.comerit.avanza.portfolio.dto.PortfolioAllocationResult;
import se.comerit.avanza.portfolio.dto.PortfolioAllocationRow;
import se.comerit.avanza.portfolio.dto.ValuedHolding;
import se.comerit.avanza.portfolio.service.PortfolioAllocationService;
import se.comerit.avanza.portfolio.service.PortfolioValuationService;
import se.comerit.avanza.targetallocation.model.TargetAllocation;
import se.comerit.avanza.targetallocation.service.TargetAllocationService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioAllocationServiceTest {

    @Mock
    private PortfolioValuationService portfolioValuationService;

    @Mock
    private TargetAllocationService targetAllocationService;

    @Test
    void getAccountTypeAllocationShouldUseSharedValuationAndTargetAllocation() {
        PortfolioAllocationService service = new PortfolioAllocationService(
                portfolioValuationService,
                targetAllocationService
        );

        when(portfolioValuationService.getValuedHoldingsByUserId(7)).thenReturn(List.of(
                valuedHolding(31, 11, "ISK", new BigDecimal("600.00")),
                valuedHolding(32, 12, "KF", new BigDecimal("400.00"))
        ));
        when(targetAllocationService.getTargetAllocationsByUserId(7)).thenReturn(List.of(
                new TargetAllocation(7, "ISK", new BigDecimal("50.00")),
                new TargetAllocation(7, "KF", new BigDecimal("50.00"))
        ));

        PortfolioAllocationResult result = service.getAccountTypeAllocation(7);

        assertEquals(new BigDecimal("1000.00"), result.totalPortfolioValueSek());
        assertTrue(result.anyDrift());

        PortfolioAllocationRow isk = row(result, "ISK");
        assertEquals(new BigDecimal("60.00"), isk.actualPercentage());
        assertEquals(new BigDecimal("50.00"), isk.targetPercentage());
        assertEquals(new BigDecimal("10.00"), isk.driftPercentagePoints());
        assertTrue(isk.overThreshold());

        PortfolioAllocationRow kf = row(result, "KF");
        assertEquals(new BigDecimal("40.00"), kf.actualPercentage());
        assertEquals(new BigDecimal("10.00"), kf.driftPercentagePoints());
        assertTrue(kf.overThreshold());

        PortfolioAllocationRow pension = row(result, "Pension");
        assertEquals(new BigDecimal("0.00"), pension.actualPercentage());
        assertFalse(pension.overThreshold());
    }

    @Test
    void exactlyFivePercentagePointsShouldNotExceedThreshold() {
        PortfolioAllocationService service = new PortfolioAllocationService(
                portfolioValuationService,
                targetAllocationService
        );

        PortfolioAllocationResult result = service.calculateAccountTypeAllocation(
                List.of(
                        valuedHolding(31, 11, "ISK", new BigDecimal("550.00")),
                        valuedHolding(32, 12, "KF", new BigDecimal("450.00"))
                ),
                List.of(
                        new TargetAllocation(7, "ISK", new BigDecimal("50.00")),
                        new TargetAllocation(7, "KF", new BigDecimal("50.00"))
                )
        );

        assertEquals(5, service.getDriftThresholdPercent());
        assertFalse(row(result, "ISK").overThreshold());
        assertFalse(row(result, "KF").overThreshold());
        assertFalse(result.anyDrift());
    }

    private PortfolioAllocationRow row(PortfolioAllocationResult result, String accountType) {
        return result.rows().stream()
                .filter(row -> accountType.equals(row.accountType()))
                .findFirst()
                .orElseThrow();
    }

    private ValuedHolding valuedHolding(
            Integer holdingId,
            Integer accountId,
            String accountType,
            BigDecimal marketValueSek) {

        return new ValuedHolding(
                holdingId,
                accountId,
                accountType,
                accountType + " account",
                holdingId + 100,
                "TEST" + holdingId,
                "Test Instrument",
                InstrumentType.STOCK,
                Sector.TECHNOLOGY,
                "SEK",
                BigDecimal.ONE,
                BigDecimal.ZERO,
                marketValueSek,
                BigDecimal.ONE.setScale(8),
                marketValueSek,
                BigDecimal.ZERO.setScale(2),
                marketValueSek,
                BigDecimal.ZERO.setScale(2)
        );
    }
}
