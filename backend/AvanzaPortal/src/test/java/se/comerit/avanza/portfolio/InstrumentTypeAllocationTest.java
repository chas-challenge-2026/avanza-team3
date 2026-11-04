package se.comerit.avanza.portfolio;

import org.junit.jupiter.api.Test;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.portfolio.dto.ValuedHolding;
import se.comerit.avanza.portfolio.service.PortfolioAllocationService;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InstrumentTypeAllocationTest {
    private final PortfolioAllocationService service = new PortfolioAllocationService(null, null);

    private ValuedHolding holding(InstrumentType type, String amount) {
        return new ValuedHolding(1, 1, "ISK", "Test", 1, "TEST", "Test", type,
                null, "SEK", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, new BigDecimal(amount), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Test
    void calculatesStockFundEtfAndOtherAndEmptyPortfolio() {
        var rows = service.calculateInstrumentTypeAllocation(List.of(
                holding(InstrumentType.STOCK, "600"), holding(InstrumentType.FUND, "200"),
                holding(InstrumentType.ETF, "100"), holding(InstrumentType.BOND, "100")));
        assertEquals(new BigDecimal("600.00"), rows.get(0).valueSek());
        assertEquals(new BigDecimal("60.00"), rows.get(0).actual());
        assertEquals(new BigDecimal("300.00"), rows.get(1).valueSek());
        assertEquals(new BigDecimal("30.00"), rows.get(1).actual());
        assertEquals(new BigDecimal("10.00"), rows.get(2).actual());
        assertEquals(new BigDecimal("0.00"), service.calculateInstrumentTypeAllocation(List.of()).get(0).actual());
    }
}
