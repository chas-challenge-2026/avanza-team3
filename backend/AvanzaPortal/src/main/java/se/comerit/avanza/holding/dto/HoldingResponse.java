package se.comerit.avanza.holding.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Holding information including instrument metadata and current market calculations")
public record HoldingResponse(

        @Schema(description = "Holding ID.", example = "12")
        Integer id,

        @Schema(description = "ID of the account containing the holding.", example = "1")
        Integer accountId,

        @Schema(description = "ID of the instrument attached to the holding.", example = "3")
        Integer instrumentId,

        @Schema(description = "Instrument ticker.", example = "AAPL")
        String ticker,

        @Schema(description = "Instrument name.", example = "Apple Inc.")
        String instrumentName,

        @Schema(description = "Frontend instrument type label.", example = "Aktie")
        String instrumentType,

        @Schema(description = "Frontend sector label.", example = "Teknik")
        String sector,

        @Schema(description = "Number of units held.", example = "10.0000")
        BigDecimal quantity,

        @Schema(description = "Average purchase price per unit.", example = "175.50")
        BigDecimal avgBuyPrice,

        @Schema(description = "Currency code of the instrument.", example = "USD")
        String currency,

        @Schema(description = "Current market price per unit.", example = "198.25")
        BigDecimal currentPrice,

        @Schema(description = "Current market value. Existing valuation logic is kept unchanged in this patch.", example = "18950.75")
        BigDecimal marketValueSek,

        @Schema(description = "Profit or loss. Existing valuation logic is kept unchanged in this patch.", example = "2180.50")
        BigDecimal pnlSek,

        @Schema(description = "Profit or loss expressed as a percentage.", example = "12.99")
        BigDecimal pnlPct
) {}
