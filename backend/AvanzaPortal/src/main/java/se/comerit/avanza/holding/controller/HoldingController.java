package se.comerit.avanza.holding.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import se.comerit.avanza.holding.dto.HoldingPatchRequest;
import se.comerit.avanza.holding.dto.HoldingRequest;
import se.comerit.avanza.holding.dto.HoldingResponse;
import se.comerit.avanza.holding.service.HoldingService;

import java.util.Map;

@RestController
@RequestMapping("/api/holdings")
public class HoldingController {

    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    @GetMapping
    public ResponseEntity<Page<Map<String, Object>>> listHoldings(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, Authentication authentication) {

        Integer userId = (Integer) authentication.getDetails();

        if (page < 0) {
            page = 0;
        }

        if (size < 1) {
            size = 20;
        }

        size = Math.min(size, 100);

        Page<Map<String, Object>> holdings = holdingService.getHoldingsByUserId(userId, page, size);

        return ResponseEntity.ok(holdings);
    }

    @PostMapping
    public ResponseEntity<Void> addHolding(@Valid @RequestBody HoldingRequest request, Authentication authentication) {

        Integer userId = (Integer) authentication.getDetails();

        holdingService.addHolding(userId, request.accountId(), request.ticker(), request.instrumentName(), request.quantity(), request.avgBuyPrice(), request.currency());

        return ResponseEntity.status(201).build();
    }

    @PatchMapping("/{holdingId}")
    public ResponseEntity<HoldingResponse> updateHolding(@PathVariable("holdingId")
                                                  Integer holdingId,
                                              @Valid @RequestBody HoldingPatchRequest request,
                                                         Authentication authentication) {
        Integer userId = (Integer) authentication.getDetails();
        return ResponseEntity.ok(holdingService.updateHolding(holdingId, userId, request));
    }


    @GetMapping("/{holdingId}")
    public ResponseEntity<HoldingResponse> getHolding(@PathVariable("holdingId")
                                                              Integer holdingId,
                                                      Authentication authentication) {
        Integer userId = (Integer) authentication.getDetails();
        return ResponseEntity.ok(holdingService.getHoldingById(holdingId, userId));
    }

    @DeleteMapping("/{holdingId}")
    public ResponseEntity<Void> deleteHolding(@PathVariable("holdingId") Integer holdingId,
            Authentication authentication) {

        Integer userId = (Integer) authentication.getDetails();
         holdingService.deleteHolding(holdingId, userId);
        return ResponseEntity.noContent().build();
    }
}
