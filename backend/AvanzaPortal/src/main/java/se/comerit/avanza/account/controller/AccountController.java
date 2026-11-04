package se.comerit.avanza.account.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import se.comerit.avanza.account.dto.AccountPatchRequest;
import se.comerit.avanza.account.dto.AccountRequest;
import se.comerit.avanza.account.dto.AccountResponse;
import se.comerit.avanza.account.service.AccountService;

import java.net.URI;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Manage the authenticated user's accounts")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "List accounts", description = "Paginated list belonging only to the logged-in user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts retrieved"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    public ResponseEntity<Page<AccountResponse>> getAccounts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1-100)") @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true) Authentication authentication) {
        Integer userId = (Integer) authentication.getDetails();
        if (page < 0) page = 0;
        if (size < 1) size = 20;
        size = Math.min(size, 100);
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId, page, size));
    }

    @Operation(summary = "Get one account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account retrieved"),
            @ApiResponse(responseCode = "404", description = "Not found or not owned by user")
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable @Positive Integer accountId,
            @Parameter(hidden = true) Authentication authentication) {
        return ResponseEntity.ok(accountService.getAccountResponse(accountId, userId(authentication)));
    }

    @Operation(summary = "Create account", description = "Multiple accounts of the same type are supported")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @NotNull @RequestBody AccountRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        AccountResponse created = accountService.createAccount(userId(authentication), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{accountId}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Replace account", description = "Replaces all editable fields (currency defaults to SEK if omitted)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account replaced"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Not found or not owned by user")
    })
    @PutMapping("/{accountId}")
    public ResponseEntity<AccountResponse> replaceAccount(
            @PathVariable @Positive Integer accountId,
            @Valid @NotNull @RequestBody AccountRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        return ResponseEntity.ok(accountService.replaceAccount(accountId, userId(authentication), request));
    }

    @Operation(summary = "Partially update account", description = "Only provided fields change")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Not found or not owned by user")
    })
    @PatchMapping("/{accountId}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable @Positive Integer accountId,
            @Valid @NotNull @RequestBody AccountPatchRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        return ResponseEntity.ok(accountService.updateAccount(accountId, userId(authentication), request));
    }

    @Operation(summary = "Delete empty account", description = "409 if account still has holdings; no holdings are deleted automatically")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account deleted"),
            @ApiResponse(responseCode = "404", description = "Not found or not owned by user"),
            @ApiResponse(responseCode = "409", description = "Account still has holdings")
    })
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable @Positive Integer accountId,
            @Parameter(hidden = true) Authentication authentication) {
        accountService.deleteAccount(accountId, userId(authentication));
        return ResponseEntity.noContent().build();
    }

    private Integer userId(Authentication authentication) {
        return (Integer) authentication.getDetails();
    }
}
