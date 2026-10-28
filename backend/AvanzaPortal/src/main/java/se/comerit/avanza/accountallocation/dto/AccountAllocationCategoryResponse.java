package se.comerit.avanza.accountallocation.dto;

import java.math.BigDecimal;
import se.comerit.avanza.accountallocation.model.AssetCategory;

public record AccountAllocationCategoryResponse(
        AssetCategory category,
        BigDecimal valueSek,
        BigDecimal actualPct,
        BigDecimal targetPct,
        BigDecimal differencePctPoints,
        BigDecimal differenceSek
) {}
