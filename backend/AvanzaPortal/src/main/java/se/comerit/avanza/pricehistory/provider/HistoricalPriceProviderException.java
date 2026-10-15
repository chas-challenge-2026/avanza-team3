package se.comerit.avanza.pricehistory.provider;

public class HistoricalPriceProviderException extends RuntimeException {
    public HistoricalPriceProviderException(String message) {
        super(message);
    }
    public HistoricalPriceProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
