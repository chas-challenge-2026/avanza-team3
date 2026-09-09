package se.comerit.avanza.dashboard.dto;

import java.math.BigDecimal;

public record HoldingSummaryResponse(
        Integer id,
        Integer accountId,
        String ticker,
        String instrumentName,
        BigDecimal quantity,
        BigDecimal avgBuyPrice,
        String currency,
        String accountType,
        String accountName,
        BigDecimal currentPrice,
        BigDecimal valueSek,
        BigDecimal unrealizedReturn,
        BigDecimal unrealizedReturnPct,
        BigDecimal sharpe,
        String displayCurrency
) {
}
