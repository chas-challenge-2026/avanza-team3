package se.comerit.avanza.market.provider;

import com.sun.jna.ptr.DoubleByReference;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeFxMarketDataProviderTest {

    @Test
    void findFxRateShouldUseNativeLibrary() {
        NativeFxMarketDataProvider provider = new NativeFxMarketDataProvider(
                (amount, from, to, outResult) -> {
                    outResult.setValue(amount * 10.50);
                    return FxLibrary.FX_OK;
                }
        );

        assertEquals(Optional.of(new BigDecimal("10.5")), provider.findFxRate("usd", "sek"));
    }

    @Test
    void findFxRateShouldReturnEmptyWhenNativeLibraryReturnsError() {
        NativeFxMarketDataProvider provider = new NativeFxMarketDataProvider(
                (amount, from, to, outResult) -> 3
        );

        assertTrue(provider.findFxRate("usd", "sek").isEmpty());
    }

    @Test
    void findFxRateShouldReturnOneForSameCurrencyWithoutCallingNativeLibrary() {
        NativeFxMarketDataProvider provider = new NativeFxMarketDataProvider(new ThrowingFxLibrary());

        assertEquals(Optional.of(BigDecimal.ONE), provider.findFxRate("sek", "SEK"));
    }

    private static class ThrowingFxLibrary implements FxLibrary {
        @Override
        public int FxConvert(double amount, String from, String to, DoubleByReference outResult) {
            throw new AssertionError("Native library should not be called for same-currency conversion");
        }
    }
}
