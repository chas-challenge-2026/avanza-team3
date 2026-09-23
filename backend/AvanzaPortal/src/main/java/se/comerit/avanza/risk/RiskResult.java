package se.comerit.avanza.risk;

import com.sun.jna.Structure;

import java.util.List;

public class RiskResult extends Structure {
    public double volatility;
    public double sharpe_ratio;
    public double max_drawdown;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("volatility", "sharpe_ratio", "max_drawdown");
    }
}
