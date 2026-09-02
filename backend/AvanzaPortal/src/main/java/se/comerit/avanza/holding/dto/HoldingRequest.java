package se.comerit.avanza.holding.dto;


import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record HoldingRequest(

        @NotBlank
        @Size(max = 20)
        String ticker,

        @NotBlank
        @Size(max = 100)
        String instrumentName,

        @NotNull
        @DecimalMin(value = "0.00001")
        @Digits(integer = 8, fraction = 4)
        BigDecimal quantity,

        @NotNull
        @DecimalMin(value = "0")
        @Digits(integer = 10, fraction = 2)
        BigDecimal avgBuyPrice,

        @NotBlank
        @Size(max = 3)
        String currency

) {}
