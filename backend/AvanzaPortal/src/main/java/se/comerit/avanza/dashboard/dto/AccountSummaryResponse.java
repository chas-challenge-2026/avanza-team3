package se.comerit.avanza.dashboard.dto;

import java.math.BigDecimal;

public record AccountSummaryResponse(
        Integer id,
        Integer userId,
        String accountType,
        String accountName,
        String currency,
        BigDecimal totalValueSek
) {
}
