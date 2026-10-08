package se.comerit.avanza.instrument.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;

public record InstrumentRequest(

        @NotBlank
        @Size(max = 20)
        String ticker,

        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        InstrumentType instrumentType,

        @NotNull
        Sector sector,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{3}$")
        String currency

) {
}