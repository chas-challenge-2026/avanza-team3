package se.comerit.avanza.rebalance.dto;

import java.math.BigDecimal;
import java.util.List;

public record RebalancePreviewResponse(
        Integer accountId,
        String proposalToken,
        BigDecimal totalValueSek,
        BigDecimal cashBeforeSek,
        BigDecimal cashAfterSek,
        boolean executable,
        List<RebalanceAllocationRow> before,
        List<RebalanceAllocationRow> after,
        List<RebalanceTradeResponse> trades,
        List<RebalanceProposedHoldingResponse> proposedHoldings,
        List<String> warnings
) {}
