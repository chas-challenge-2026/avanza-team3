package se.comerit.avanza.accountallocation.dto;

import java.math.BigDecimal;
import java.util.List;

public record AccountAllocationOverviewResponse(
        Integer accountId,
        BigDecimal totalValueSek,
        boolean targetsConfigured,
        List<AccountAllocationCategoryResponse> categories
) {}
