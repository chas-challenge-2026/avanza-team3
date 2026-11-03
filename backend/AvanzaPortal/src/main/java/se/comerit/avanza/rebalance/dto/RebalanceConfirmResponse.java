package se.comerit.avanza.rebalance.dto;

import java.math.BigDecimal;
import java.util.List;

public record RebalanceConfirmResponse(
        Integer accountId,
        String status,
        BigDecimal cashBalanceSek,
        List<RebalanceTradeResponse> executedTrades
) {}
