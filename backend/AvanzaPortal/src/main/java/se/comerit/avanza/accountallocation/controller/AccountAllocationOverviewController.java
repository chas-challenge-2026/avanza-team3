package se.comerit.avanza.accountallocation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.comerit.avanza.accountallocation.dto.AccountAllocationOverviewResponse;
import se.comerit.avanza.accountallocation.service.AccountAllocationOverviewService;

@RestController
@RequestMapping("/api/accounts/{accountId}/allocation")
@Tag(name = "Account Allocation", description = "Actual versus target allocation for one owned account")
@SecurityRequirement(name = "bearerAuth")
public class AccountAllocationOverviewController {
    private final AccountAllocationOverviewService service;

    public AccountAllocationOverviewController(AccountAllocationOverviewService service) {
        this.service = service;
    }

    @Operation(summary = "Get actual asset-category allocation and deviation from per-account targets")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account allocation returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Account not found or not owned by user")
    })
    @GetMapping
    public ResponseEntity<AccountAllocationOverviewResponse> getAllocation(
            @PathVariable @Positive Integer accountId, Authentication authentication) {
        Integer userId = (Integer) authentication.getDetails();
        return ResponseEntity.ok(service.getAllocation(accountId, userId));
    }
}
