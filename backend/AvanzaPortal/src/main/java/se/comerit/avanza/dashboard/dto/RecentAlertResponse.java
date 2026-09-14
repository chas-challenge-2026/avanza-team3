package se.comerit.avanza.dashboard.dto;

import java.time.LocalDateTime;

public record RecentAlertResponse(
        Integer id,
        String alertType,
        String message,
        LocalDateTime createdAt
) {
}
