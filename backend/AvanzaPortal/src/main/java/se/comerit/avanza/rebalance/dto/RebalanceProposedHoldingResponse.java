package se.comerit.avanza.rebalance.dto;

import java.math.BigDecimal;
import se.comerit.avanza.accountallocation.model.AssetCategory;

public record RebalanceProposedHoldingResponse(
        Integer instrumentId,
        String ticker,
        String instrumentName,
        AssetCategory category,
        BigDecimal currentQuantity,
        BigDecimal proposedQuantity,
        BigDecimal unitPriceSek,
        BigDecimal currentValueSek,
        BigDecimal proposedValueSek
) { }
