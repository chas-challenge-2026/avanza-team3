package se.comerit.avanza.holding.dto;

import java.math.BigDecimal;

public record HoldingResponse(
        Integer id,
        Integer accountId,
        String ticker,
        String instrumentName,
        BigDecimal quantity,
        BigDecimal avgBuyPrice,
        String currency
//        BigDecimal currentPrice,
//        BigDecimal marketValueSek,
//        BigDecimal pnlSek,
//        BigDecimal pnlPct
) {
}
