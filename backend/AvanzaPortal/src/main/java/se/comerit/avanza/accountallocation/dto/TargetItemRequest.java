package se.comerit.avanza.accountallocation.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import se.comerit.avanza.accountallocation.model.AssetCategory;

public record TargetItemRequest(
        @NotNull
        AssetCategory category,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("100.00")
        @Digits(integer = 3, fraction = 2)
        BigDecimal targetPercentage
) {}
