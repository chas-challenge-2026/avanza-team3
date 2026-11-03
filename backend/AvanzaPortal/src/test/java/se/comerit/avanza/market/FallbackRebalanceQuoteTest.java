package se.comerit.avanza.market;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import se.comerit.avanza.accountallocation.model.AssetCategory;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.market.provider.FallbackMarketDataProvider;
import static org.junit.jupiter.api.Assertions.*;

class FallbackRebalanceQuoteTest {
    private final FallbackMarketDataProvider provider = new FallbackMarketDataProvider();

    @Test
    void demoSeedTickersHaveDedicatedSimulationPrices() {
        assertEquals(new BigDecimal("125.00"), provider.findPrice("FND-SE").orElseThrow());
        assertEquals(new BigDecimal("200.00"), provider.findPrice("FND-WORLD").orElseThrow());
        assertEquals(new BigDecimal("100.00"), provider.findPrice("GOV-SE").orElseThrow());
        assertEquals(new BigDecimal("150.00"), provider.findPrice("GOLD-ETF").orElseThrow());
    }

    @Test
    void commodityEtfRemainsInFundCategoryAndBondHasItsOwnCategory() {
        assertEquals(AssetCategory.FUND, AssetCategory.fromInstrumentType(InstrumentType.ETF));
        assertEquals(AssetCategory.BOND, AssetCategory.fromInstrumentType(InstrumentType.BOND));
    }
}
