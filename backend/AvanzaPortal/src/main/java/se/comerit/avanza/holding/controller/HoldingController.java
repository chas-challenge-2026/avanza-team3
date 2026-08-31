package se.comerit.avanza.holding.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.comerit.avanza.holding.dto.HoldingRequest;
import se.comerit.avanza.holding.service.HoldingService;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/holdings")
public class HoldingController {

    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listHoldings(HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<Map<String, Object>> holdings = holdingService.getHoldingsByUserId(userId);
        return ResponseEntity.ok(holdings);
        // Fetch all holdings — no pagination, no LIMIT
        // This will load all rows into memory. Fine for small datasets. Definitely fine.
    }

    @PostMapping
    public ResponseEntity<Void> addHolding(@Valid @RequestBody HoldingRequest request, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        holdingService.addHolding(userId, request.accountId(), request.ticker(), request.instrumentName(), request.quantity(), request.avgBuyPrice(), request.currency());

        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{holdingId}")
    public ResponseEntity<Void> deleteHolding(@PathVariable("holdingId") Integer holdingId,
                                HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
         holdingService.deleteHolding(holdingId, userId);
        return ResponseEntity.noContent().build();
    }
}
