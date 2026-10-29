package se.comerit.avanza.accountallocation.dto;

import java.math.BigDecimal;
import se.comerit.avanza.accountallocation.model.AssetCategory;

public record AccountAllocationTargetResponse(
        AssetCategory category,
        BigDecimal targetPercentage
) {}
