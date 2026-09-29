package se.comerit.avanza.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated holdings included in the portfolio dashboard")
public record HoldingPageResponse(

        @Schema(description = "Holdings on the current page")
        List<HoldingSummaryResponse> content,

        @Schema(description = "Current zero-based page number", example = "0")
        int page,

        @Schema(description = "Requested page size", example = "20")
        int size,

        @Schema(description = "Total number of holdings", example = "5")
        long totalElements,

        @Schema(description = "Total number of pages", example = "1")
        int totalPages,

        @Schema(description = "Whether this is the first page", example = "true")
        boolean first,

        @Schema(description = "Whether this is the last page", example = "true")
        boolean last
) {
}
