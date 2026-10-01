package se.comerit.avanza.holding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;

import java.math.BigDecimal;

@Schema(description = "Holding information including instrument metadata and current market calculations")
public record HoldingResponse(

        @Schema(
                description = "Holding ID.",
                example = "12"
        )
        Integer id,

        @Schema(
                description = "ID of the account containing the holding.",
                example = "1"
        )
        Integer accountId,

        @Schema(
                description = "ID of the instrument attached to the holding.",
                example = "3"
        )
        Integer instrumentId,

        @Schema(
                description = "Instrument ticker.",
                example = "AAPL"
        )
        String ticker,

        @Schema(
                description = "Instrument name.",
                example = "Apple Inc."
        )
        String instrumentName,

        @Schema(
                description = "Instrument type.",
                example = "STOCK"
        )
        InstrumentType instrumentType,


        @Schema(
                description = "Instrument sector.",
                example = "TECHNOLOGY"
        )
        Sector sector,

        @Schema(
                description = "Number of units held.",
                example = "10.0000"
        )
        BigDecimal quantity,

        @Schema(
                description = "Average purchase price per unit.",
                example = "175.50"
        )
        BigDecimal avgBuyPrice,

        @Schema(
                description = "Currency code of the instrument.",
                example = "USD"
        )
        String currency,

        @Schema(
                description = "Current market price per unit.",
                example = "198.25"
        )
        BigDecimal currentPrice,

        @Schema(
                description = "Current market value converted to SEK.",
                example = "18950.75"
        )
        BigDecimal marketValueSek,

        @Schema(
                description = "Profit or loss expressed in SEK.",
                example = "2180.50"
        )
        BigDecimal pnlSek,

        @Schema(
                description = "Profit or loss expressed as a percentage.",
                example = "12.99"
        )
        BigDecimal pnlPct
) {
}
