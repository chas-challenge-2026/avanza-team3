package se.comerit.avanza.alert.dto;

import java.math.BigDecimal;

public record LiveAlertDto( 
    String alertType,
    Integer accountId,
    String accountName,
    String category,
    BigDecimal actualPct,
    BigDecimal targetPct,
    BigDecimal differencePctPoints,
    String message,
    boolean dismissed,
    String createdAt
){}
