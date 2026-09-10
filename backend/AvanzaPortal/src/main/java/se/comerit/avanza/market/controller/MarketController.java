package se.comerit.avanza.market.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.comerit.avanza.market.dto.FxRateResponse;
import se.comerit.avanza.market.dto.PriceResponse;
import se.comerit.avanza.market.service.MarketDataService;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketDataService marketDataService;

    public MarketController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/price/{ticker}")
    public ResponseEntity<PriceResponse> getPrice(@PathVariable String ticker) {
        return ResponseEntity.ok(new PriceResponse(
                ticker.toUpperCase(),
                marketDataService.getPrice(ticker)
        ));
    }

    @GetMapping("/fx/{from}/{to}")
    public ResponseEntity<FxRateResponse> getFxRate(@PathVariable String from, @PathVariable String to) {
        return ResponseEntity.ok(new FxRateResponse(
                from.toUpperCase(),
                to.toUpperCase(),
                marketDataService.getFxRate(from, to)
        ));
    }
}
