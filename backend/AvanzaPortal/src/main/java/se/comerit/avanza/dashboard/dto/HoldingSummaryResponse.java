package se.comerit.avanza.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Enriched holding information used by the portfolio dashboard")
public record HoldingSummaryResponse(

        @Schema(description = "Holding ID", example = "12")
        Integer id,

        @Schema(description = "Account ID", example = "1")
        Integer accountId,

        @Schema(description = "Instrument ticker", example = "AAPL")
        String ticker,

        @Schema(description = "Instrument name", example = "Apple Inc.")
        String instrumentName,

        @Schema(description = "Number of units held", example = "10.0000")
        BigDecimal quantity,

        @Schema(description = "Average purchase price per unit", example = "175.50")
        BigDecimal avgBuyPrice,

        @Schema(description = "Holding currency", example = "USD")
        String currency,

        @Schema(description = "Account type", example = "ISK")
        String accountType,

        @Schema(description = "Account display name", example = "Main ISK")
        String accountName,

        @Schema(description = "Current market price per unit", example = "187.32")
        BigDecimal currentPrice,

        @Schema(description = "Current holding value converted to SEK", example = "19575.00")
        BigDecimal valueSek,

        @Schema(description = "Unrealized profit or loss converted to SEK", example = "1235.00")
        BigDecimal unrealizedReturn,

        @Schema(description = "Unrealized return in percent", example = "6.73")
        BigDecimal unrealizedReturnPct,

        @Schema(description = "Current dashboard Sharpe estimate", example = "0.31")
        BigDecimal sharpe,

        @Schema(description = "How the displayed value was converted", example = "USD->SEK")
        String displayCurrency
) {
}
