package se.comerit.avanza.rebalance;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import se.comerit.avanza.accountallocation.model.AssetCategory;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.portfolio.dto.ValuedHolding;
import se.comerit.avanza.rebalance.service.RebalanceCalculator;

import static org.junit.jupiter.api.Assertions.*;

class RebalanceCalculatorTest {
    private final RebalanceCalculator calculator = new RebalanceCalculator();

    private Instrument instrument(String ticker, InstrumentType type) {
        return new Instrument(ticker, ticker, type, null, "SEK");
    }

    private ValuedHolding holding(int id, int instrumentId, InstrumentType type, String quantity) {
        BigDecimal qty = new BigDecimal(quantity);
        BigDecimal value = qty.multiply(new BigDecimal("100.00"));
        return new ValuedHolding(id, 11, "ISK", "ISK 1", instrumentId, "TICK" + instrumentId,
                "Test", type, null, "SEK", qty, new BigDecimal("100.00"),
                new BigDecimal("100.00"), BigDecimal.ONE, value, value,
                BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private Map<AssetCategory, BigDecimal> targets(String stock, String fund) {
        Map<AssetCategory, BigDecimal> map = new EnumMap<>(AssetCategory.class);
        map.put(AssetCategory.STOCK, new BigDecimal(stock));
        map.put(AssetCategory.FUND, new BigDecimal(fund));
        return map;
    }

    private RebalanceCalculator.Quote quote(Instrument i) {
        return new RebalanceCalculator.Quote(i, new BigDecimal("100.00"), BigDecimal.ONE);
    }

    @Test
    void sellsOverweightStockAndBuysApprovedFundWithoutLosingCash() {
        var stock = instrument("STOCK", InstrumentType.STOCK);
        var fund = instrument("FUND", InstrumentType.FUND);
        // IDs are assigned in database. Here we use real Instrument DTO constructor and test IDs via reflection.
        org.springframework.test.util.ReflectionTestUtils.setField(stock, "id", 1);
        org.springframework.test.util.ReflectionTestUtils.setField(fund, "id", 2);
        var result = calculator.calculate(BigDecimal.ZERO,
                List.of(holding(11, 1, InstrumentType.STOCK, "7"),
                        holding(12, 2, InstrumentType.FUND, "3")),
                List.of(quote(stock), quote(fund)), targets("50.00", "50.00"));
        assertTrue(result.executable());
        assertEquals(2, result.trades().size());
        assertEquals("SELL", result.trades().get(0).side());
        assertEquals("BUY", result.trades().get(1).side());
        assertEquals(new BigDecimal("2"), result.trades().get(0).quantity());
        assertEquals(new BigDecimal("2.0000"), result.trades().get(1).quantity());
        assertEquals(new BigDecimal("0.00"), result.cashAfterSek());
        assertEquals(new BigDecimal("50.00"), result.after().get(0).actualPct());
        assertEquals(new BigDecimal("50.00"), result.after().get(1).actualPct());
    }

    @Test
    void unapprovedFundPreventsAnUnhelpfulSaleToCash() {
        var stock = instrument("STOCK", InstrumentType.STOCK);
        org.springframework.test.util.ReflectionTestUtils.setField(stock, "id", 1);
        var result = calculator.calculate(BigDecimal.ZERO,
                List.of(holding(11, 1, InstrumentType.STOCK, "7"),
                        holding(12, 2, InstrumentType.FUND, "3")),
                List.of(quote(stock)), targets("50.00", "50.00"));
        assertFalse(result.executable());
        assertTrue(result.trades().isEmpty());
        assertEquals(result.before(), result.after());
    }

    @Test
    void allowsPurchasingAnApprovedInstrumentNotYetHeld() {
        var stock = instrument("STOCK", InstrumentType.STOCK);
        var fund = instrument("FUND", InstrumentType.FUND);
        org.springframework.test.util.ReflectionTestUtils.setField(stock, "id", 1);
        org.springframework.test.util.ReflectionTestUtils.setField(fund, "id", 2);
        var result = calculator.calculate(BigDecimal.ZERO,
                List.of(holding(11, 1, InstrumentType.STOCK, "10")),
                List.of(quote(stock), quote(fund)), targets("50.00", "50.00"));
        assertTrue(result.executable());
        assertEquals(2, result.trades().size());
        assertEquals(2, result.trades().get(1).instrumentId());
        assertEquals(new BigDecimal("5.0000"), result.trades().get(1).quantity());
    }

    @Test
    void usesAvailableCashWithoutShortSelling() {
        var stock = instrument("STOCK", InstrumentType.STOCK);
        org.springframework.test.util.ReflectionTestUtils.setField(stock, "id", 1);
        var goals = new EnumMap<AssetCategory, BigDecimal>(AssetCategory.class);
        goals.put(AssetCategory.STOCK, new BigDecimal("100.00"));
        var result = calculator.calculate(new BigDecimal("200.00"),
                List.of(holding(11, 1, InstrumentType.STOCK, "3")),
                List.of(quote(stock)), goals);
        assertTrue(result.executable());
        assertEquals("BUY", result.trades().getFirst().side());
        assertEquals(new BigDecimal("0.00"), result.cashAfterSek());
    }
}
