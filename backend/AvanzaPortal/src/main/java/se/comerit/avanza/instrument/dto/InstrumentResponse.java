package se.comerit.avanza.instrument.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;

@Schema(description = "Financial instrument information")
public record InstrumentResponse(

        @Schema(
                description = "Instrument ID.",
                example = "3"
        )
        Integer id,

        @Schema(
                description = "Instrument ticker.",
                example = "AAPL"
        )
        String ticker,

        @Schema(
                description = "Instrument name.",
                example = "Apple Inc."
        )
        String name,

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
                description = "Three-letter currency code.",
                example = "USD"
        )
        String currency
) {
}
