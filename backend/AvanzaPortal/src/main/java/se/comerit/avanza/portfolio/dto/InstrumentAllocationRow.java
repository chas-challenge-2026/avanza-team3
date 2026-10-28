package se.comerit.avanza.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Portfolio allocation by instrument category")
public record InstrumentAllocationRow(

        @Schema(description = "Instrument category",
                example = "Aktie")
        String instrumentType,

        @Schema(description = "Market value in SEK",
                example = "150000.00")
        BigDecimal valueSek,

        @Schema(description = "Percentage of total portfolio value",
                example = "60.00")
        BigDecimal actual
) {}