package se.comerit.avanza.instrument.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;

@Schema(description = "Data required to create a financial instrument")
public record InstrumentRequest(

        @Schema(
                description = "Unique instrument ticker. The backend normalizes the value to uppercase.",
                example = "AAPL"
        )
        @NotBlank
        @Size(max = 20)
        String ticker,

        @Schema(
                description = "Display name of the instrument.",
                example = "Apple Inc."
        )
        @NotBlank
        @Size(max = 100)
        String name,

        @Schema(
                description = "Type of financial instrument.",
                example = "STOCK"
        )
        @NotNull
        InstrumentType instrumentType,

        @Schema(
                description = "Sector associated with the instrument.",
                example = "TECHNOLOGY"
        )
        @NotNull
        Sector sector,

        @Schema(
                description = "Three-letter currency code. The backend normalizes the value to uppercase.",
                example = "USD"
        )
        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{3}$")
        String currency

) {
}
