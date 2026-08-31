package se.comerit.avanza.holding.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record HoldingRequest(

        @NotNull
        Integer accountId,

        @NotBlank
        @Size(max = 30)
        String ticker,

        @NotBlank
        @Size(max = 100)
        String instrumentName,

        @NotNull
        @DecimalMin(value = "0.00000001")
        BigDecimal quantity,

        @NotNull
        @DecimalMin(value = "0")
        BigDecimal avgBuyPrice,

        @NotBlank
        @Size(max = 3)
        String currency

) {}
