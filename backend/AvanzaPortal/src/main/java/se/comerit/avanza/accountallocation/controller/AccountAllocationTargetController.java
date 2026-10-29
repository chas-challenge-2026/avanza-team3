package se.comerit.avanza.accountallocation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import se.comerit.avanza.accountallocation.dto.AccountAllocationTargetsResponse;
import se.comerit.avanza.accountallocation.dto.ReplaceAccountTargetsRequest;
import se.comerit.avanza.accountallocation.service.AccountAllocationTargetService;

@RestController
@RequestMapping("/api/accounts/{accountId}/allocation-targets")
@Tag(name = "Account Allocation Targets", description = "Asset-category goals for one owned account")
@SecurityRequirement(name = "bearerAuth")
public class AccountAllocationTargetController {
    private final AccountAllocationTargetService targetService;

    public AccountAllocationTargetController(AccountAllocationTargetService targetService) {
        this.targetService = targetService;
    }

    @Operation(summary = "Get target allocation for one account")
    @GetMapping
    public ResponseEntity<AccountAllocationTargetsResponse> getTargets(
            @PathVariable @Positive Integer accountId, Authentication authentication) {
        return ResponseEntity.ok(targetService.getTargets(accountId, userId(authentication)));
    }

    @Operation(summary = "Atomically replace all category targets (must sum to 100%)")
    @PutMapping
    public ResponseEntity<AccountAllocationTargetsResponse> replaceTargets(
            @PathVariable @Positive Integer accountId,
            @Valid @NotNull @RequestBody ReplaceAccountTargetsRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(targetService.replaceTargets(accountId, userId(authentication), request));
    }

    @Operation(summary = "Delete target allocation for one account")
    @DeleteMapping
    public ResponseEntity<Void> deleteTargets(
            @PathVariable @Positive Integer accountId, Authentication authentication) {
        targetService.deleteTargets(accountId, userId(authentication));
        return ResponseEntity.noContent().build();
    }

    private Integer userId(Authentication authentication) {
        return (Integer) authentication.getDetails();
    }
}
