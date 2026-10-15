package se.comerit.avanza.market.provider;

import com.sun.jna.Library;
import com.sun.jna.ptr.DoubleByReference;

public interface FxLibrary extends Library {
    int FX_OK = 0;

    int FxConvert(double amount, String from, String to, DoubleByReference outResult);
}
