package se.comerit.avanza.holding.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.comerit.avanza.holding.dto.HoldingRequest;
import se.comerit.avanza.holding.service.HoldingService;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HoldingController {

    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    @GetMapping("/accounts/{accountId}/holdings")
    public ResponseEntity<Page<Map<String, Object>>> listHoldings(
            @PathVariable Integer accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        if (page < 0) {
            page = 0;
        }

        if (size < 1) {
            size = 20;
        }

        size = Math.min(size, 100);

        Page<Map<String, Object>> holdings = holdingService.getHoldingsByAccountIdAndUserId(accountId, userId, page, size);

        return ResponseEntity.ok(holdings);
    }

    @PostMapping("/accounts/{accountId}/holdings")
    public ResponseEntity<Void> addHolding(
            @PathVariable Integer accountId,
            @Valid @RequestBody HoldingRequest request,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        holdingService.addHolding(userId, accountId, request.ticker(), request.instrumentName(), request.quantity(), request.avgBuyPrice(), request.currency());

        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/holdings/{holdingId}")
    public ResponseEntity<Void> deleteHolding(@PathVariable Integer holdingId,
                                HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
         holdingService.deleteHolding(holdingId, userId);
        return ResponseEntity.noContent().build();
    }
}
