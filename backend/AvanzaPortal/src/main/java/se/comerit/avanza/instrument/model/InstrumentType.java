package se.comerit.avanza.instrument.model;

public enum InstrumentType {
    STOCK("Aktie", false),
    FUND("Fond", true),
    ETF("Fond", true),
    BOND("Övrigt", false),
    OPTION("Övrigt", false),
    UNKNOWN("Övrigt", false);

    private final String displayName;
    private final boolean aFund;

    InstrumentType(String displayName, boolean aFund) {
        this.displayName = displayName;
        this.aFund = aFund;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isFund() {
        return aFund;
    }
}
