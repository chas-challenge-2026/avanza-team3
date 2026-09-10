package se.comerit.avanza.holding.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
public record HoldingPatchRequest(

        @Size(max = 20)
        String ticker,

        @Size(max = 100)
        String instrumentName,

        @DecimalMin(value = "0.00001")
        @Digits(integer = 8, fraction = 4)
        BigDecimal quantity,

        @DecimalMin(value = "0")
        @Digits(integer = 10, fraction = 2)
        BigDecimal avgBuyPrice,

        @Size(max = 3)
        String currency
) {}
