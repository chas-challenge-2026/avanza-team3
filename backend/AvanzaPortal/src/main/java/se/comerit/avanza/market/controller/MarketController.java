package se.comerit.avanza.market.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Market Data",
        description = "Endpoints for instrument prices and foreign-exchange rates"
)
@SecurityRequirement(name = "bearerAuth")
public class MarketController {

    private final MarketDataService marketDataService;

    public MarketController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @Operation(
            summary = "Get instrument price",
            description = "Returns the current price supplied by the configured market-data providers."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Price retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/price/{ticker}")
    public ResponseEntity<PriceResponse> getPrice(
            @Parameter(description = "Instrument ticker", example = "AAPL")
            @PathVariable String ticker) {

        return ResponseEntity.ok(new PriceResponse(
                ticker.toUpperCase(),
                marketDataService.getPrice(ticker)
        ));
    }

    @Operation(
            summary = "Get FX rate",
            description = "Returns the exchange rate from one currency to another using the configured market-data providers."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "FX rate retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/fx/{from}/{to}")
    public ResponseEntity<FxRateResponse> getFxRate(
            @Parameter(description = "Source currency code", example = "USD")
            @PathVariable String from,

            @Parameter(description = "Target currency code", example = "SEK")
            @PathVariable String to) {

        return ResponseEntity.ok(new FxRateResponse(
                from.toUpperCase(),
                to.toUpperCase(),
                marketDataService.getFxRate(from, to)
        ));
    }
}
