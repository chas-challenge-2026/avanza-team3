package se.comerit.avanza.instrument.model;

public enum Sector {
    TECHNOLOGY("Teknik"),
    FINANCIALS("Finans"),
    INDUSTRIALS("Industri"),
    HEALTHCARE("Hälsovård"),
    CONSUMER_DISCRETIONARY("Övrigt"),
    COMMUNICATION_SERVICES("Övrigt"),
    CONSUMER_STAPLES("Övrigt"),
    MATERIALS("Övrigt"),
    REAL_ESTATE("Fastigheter"),
    UTILITIES("Övrigt"),
    ENERGY("Övrigt"),
    UNKNOWN("Övrigt");

    private final String displayName;

    Sector(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
