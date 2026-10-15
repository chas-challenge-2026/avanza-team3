package se.comerit.avanza.pricehistory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import se.comerit.avanza.pricehistory.dto.PriceHistoryResponse;
import se.comerit.avanza.pricehistory.service.HistoricalPriceService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/price-history/instruments/{instrumentId}")
@Tag(name = "Price History", description = "Stored daily prices and explicit provider imports")
@SecurityRequirement(name = "bearerAuth")
public class HistoricalPriceController {
    private final HistoricalPriceService service;

    public HistoricalPriceController(HistoricalPriceService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Read stored daily prices, oldest first")
    public PriceHistoryResponse getHistory(
            @PathVariable Integer instrumentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.getHistory(instrumentId, from, to);
    }

    @PostMapping("/import")
    @Operation(summary = "Import daily prices from the configured provider",
            description = "Reimporting a date range updates existing prices. Stub mode imports no data.")
    public PriceHistoryResponse importHistory(
            @PathVariable Integer instrumentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.importHistory(instrumentId, from, to);
    }
}
