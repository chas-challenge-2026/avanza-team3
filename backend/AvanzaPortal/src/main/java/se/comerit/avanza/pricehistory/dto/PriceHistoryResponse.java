package se.comerit.avanza.pricehistory.dto;

import java.util.List;

public record PriceHistoryResponse(
        Integer instrumentId,
        String ticker,
        String configuredProvider,
        List<HistoricalPricePoint> prices
) {}
