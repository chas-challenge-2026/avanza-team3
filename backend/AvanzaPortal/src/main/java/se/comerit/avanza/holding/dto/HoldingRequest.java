package se.comerit.avanza.holding.dto;

import java.math.BigDecimal;

public record HoldingRequest(

        Integer accountId,

        String ticker,

        String instrumentName,

        BigDecimal quantity,

        BigDecimal avgBuyPrice,

        String currency

) {}
