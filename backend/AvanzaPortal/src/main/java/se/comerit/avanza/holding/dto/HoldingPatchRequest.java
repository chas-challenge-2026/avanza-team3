package se.comerit.avanza.holding.dto;

import java.math.BigDecimal;
//OBS!!! GLÖM EJ VALIDERING SENARE!!!!!
public record HoldingPatchRequest(
        String ticker,
        String instrumentName,
        BigDecimal quantity,
        BigDecimal avgBuyPrice,
        String currency
) {}
