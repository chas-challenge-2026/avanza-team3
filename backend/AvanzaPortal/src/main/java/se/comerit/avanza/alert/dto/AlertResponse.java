package se.comerit.avanza.alert.dto;

import java.time.LocalDateTime;

public record AlertResponse(
        Integer id,
        String alertType,
        String message,
        boolean dismissed,
        LocalDateTime createdAt
) {}
