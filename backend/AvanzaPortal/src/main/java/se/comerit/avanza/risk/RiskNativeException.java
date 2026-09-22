package se.comerit.avanza.risk;

public class RiskNativeException extends RuntimeException {
    private final int status;

    public RiskNativeException(int status, String message) {
        super("risk_compute failed with status " + status + ": " + message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
