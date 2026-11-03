package se.comerit.avanza.rebalance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import se.comerit.avanza.rebalance.dto.*;
import se.comerit.avanza.rebalance.service.RebalanceService;

@RestController
@RequestMapping("/api/accounts/{accountId}/rebalance")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Rebalancing", description = "Simulation-only per-account rebalance preview and confirmation")
public class RebalanceController {
    private final RebalanceService service;
    public RebalanceController(RebalanceService service) { this.service = service; }

    @Operation(summary = "Preview simulated trades; no database writes")
    @PostMapping("/preview")
    public ResponseEntity<RebalancePreviewResponse> preview(
            @PathVariable @Positive Integer accountId,
            @Valid @RequestBody RebalancePreviewRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(service.preview(accountId, (Integer) authentication.getDetails(), request));
    }

    @Operation(summary = "Confirm a fresh preview; applies all changes in one transaction")
    @PostMapping("/confirm")
    public ResponseEntity<RebalanceConfirmResponse> confirm(
            @PathVariable @Positive Integer accountId,
            @Valid @RequestBody RebalanceConfirmRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(service.confirm(accountId, (Integer) authentication.getDetails(), request));
    }
}
