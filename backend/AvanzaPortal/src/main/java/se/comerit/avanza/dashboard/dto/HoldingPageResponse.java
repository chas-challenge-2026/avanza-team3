package se.comerit.avanza.dashboard.dto;

import java.util.List;

public record HoldingPageResponse(
        List<HoldingSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
