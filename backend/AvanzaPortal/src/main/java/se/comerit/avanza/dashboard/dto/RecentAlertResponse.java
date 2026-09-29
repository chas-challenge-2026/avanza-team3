package se.comerit.avanza.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Non-dismissed alert displayed in the portfolio dashboard")
public record RecentAlertResponse(

        @Schema(description = "Alert ID", example = "42")
        Integer id,

        @Schema(description = "Alert type", example = "DRIFT")
        String alertType,

        @Schema(description = "Alert message", example = "ISK allocation differs from the target allocation")
        String message,

        @Schema(description = "Time when the alert was created", example = "2026-09-17T10:30:00")
        LocalDateTime createdAt
) {
}
