package se.comerit.avanza.rebalance.dto;

import java.math.BigDecimal;
import se.comerit.avanza.accountallocation.model.AssetCategory;

public record RebalanceTradeResponse(
        String side,
        Integer instrumentId,
        String ticker,
        AssetCategory category,
        BigDecimal quantity,
        BigDecimal unitPriceSek,
        BigDecimal amountSek
) {}
