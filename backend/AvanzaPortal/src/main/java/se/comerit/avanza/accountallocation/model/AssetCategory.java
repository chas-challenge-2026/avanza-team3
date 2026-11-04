package se.comerit.avanza.accountallocation.model;

import se.comerit.avanza.instrument.model.InstrumentType;

public enum AssetCategory {
    STOCK, FUND, BOND, OTHER;

    public static AssetCategory fromInstrumentType(InstrumentType instrumentType) {
        if (instrumentType == null) return OTHER;
        return switch (instrumentType) {
            case STOCK -> STOCK;
            case FUND, ETF -> FUND;
            case BOND -> BOND;
            case OPTION, UNKNOWN -> OTHER;
        };
    }
}
