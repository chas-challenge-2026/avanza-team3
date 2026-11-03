package se.comerit.avanza.rebalance.dto;

import java.math.BigDecimal;
import se.comerit.avanza.accountallocation.model.AssetCategory;

public record RebalanceAllocationRow(
        AssetCategory category,
        BigDecimal valueSek,
        BigDecimal actualPct,
        BigDecimal targetPct,
        BigDecimal differencePctPoints
) {}
