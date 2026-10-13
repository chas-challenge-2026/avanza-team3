package se.comerit.avanza.market.provider;

import com.sun.jna.ptr.DoubleByReference;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import se.comerit.avanza.nativebridge.NativeLibraryLoader;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Component
@Order(0)
public class NativeFxMarketDataProvider implements MarketDataProvider {

    private final Supplier<FxLibrary> fxLibrarySupplier;

    public NativeFxMarketDataProvider() {
        this(() -> FxLibraryHolder.INSTANCE);
    }

    NativeFxMarketDataProvider(FxLibrary fxLibrary) {
        this(() -> fxLibrary);
    }

    private NativeFxMarketDataProvider(Supplier<FxLibrary> fxLibrarySupplier) {
        this.fxLibrarySupplier = Objects.requireNonNull(fxLibrarySupplier, "fxLibrarySupplier");
    }

    @Override
    public Optional<BigDecimal> findPrice(String ticker) {
        return Optional.empty();
    }

    @Override
    public Optional<BigDecimal> findFxRate(String fromCurrency, String toCurrency) {
        String from = normalizeCurrency(fromCurrency);
        String to = normalizeCurrency(toCurrency);
        if (from.equals(to)) {
            return Optional.of(BigDecimal.ONE);
        }

        DoubleByReference result = new DoubleByReference();
        try {
            int status = fxLibrarySupplier.get().FxConvert(1.0, from, to, result);
            double rate = result.getValue();
            if (status != FxLibrary.FX_OK || !Double.isFinite(rate) || rate <= 0.0) {
                return Optional.empty();
            }

            return Optional.of(BigDecimal.valueOf(rate));
        } catch (UnsatisfiedLinkError | NoClassDefFoundError exception) {
            return Optional.empty();
        }
    }

    private String normalizeCurrency(String currency) {
        return currency.toUpperCase(Locale.ROOT);
    }

    private static class FxLibraryHolder {
        private static final FxLibrary INSTANCE = NativeLibraryLoader.load("fx", FxLibrary.class);
    }
}
