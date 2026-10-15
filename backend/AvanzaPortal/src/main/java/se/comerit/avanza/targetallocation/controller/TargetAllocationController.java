package se.comerit.avanza.targetallocation.controller;

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
import se.comerit.avanza.targetallocation.dto.TargetAllocationRequest;
import se.comerit.avanza.targetallocation.dto.TargetAllocationResponse;
import se.comerit.avanza.targetallocation.service.TargetAllocationService;

@RestController
@RequestMapping("/api/target-allocations")
@Tag(
        name = "Target Allocations",
        description = "Endpoints for viewing and managing the authenticated user's target allocations"
)
@SecurityRequirement(name = "bearerAuth")
public class TargetAllocationController {

    private final TargetAllocationService targetAllocationService;

    public TargetAllocationController(TargetAllocationService targetAllocationService) {
        this.targetAllocationService = targetAllocationService;
    }

    @Operation(
            summary = "Get target allocations",
            description = "Returns a paginated list of target allocations belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Target allocations retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    public ResponseEntity<Page<TargetAllocationResponse>> getTargetAllocations(
            @Parameter(
                    description = "Page number. Page numbering starts at 0.",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Number of target allocations per page. Default is 20 and maximum is 100.",
                    example = "20"
            )
            @RequestParam(defaultValue = "20") int size,

            @Parameter(hidden = true)
            Authentication authentication) {

        Integer userId = (Integer) authentication.getDetails();

        if (page < 0) {
            page = 0;
        }

        if (size < 1) {
            size = 20;
        }

        size = Math.min(size, 100);
        return ResponseEntity.ok(
                targetAllocationService.getTargetAllocationsByUserId(
                        userId,
                        page,
                        size));
    }

    @Operation(
            summary = "Get target allocation by ID",
            description = "Returns a target allocation if it belongs to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Target allocation retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Target allocation not found")
    })
    @GetMapping("/{targetAllocationId}")
    public ResponseEntity<TargetAllocationResponse> getTargetAllocation(
            @PathVariable("targetAllocationId") @Positive Integer targetAllocationId,
            @Parameter(hidden = true) Authentication authentication) {

        Integer userId = (Integer) authentication.getDetails();
        return ResponseEntity.ok(
                targetAllocationService.getTargetAllocationResponse(targetAllocationId, userId)
        );
    }

    @Operation(
            summary = "Create target allocation",
            description = "Creates a target allocation for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Target allocation created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid target allocation data"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "409", description = "Target allocation already exists for the account type")
    })
    @PostMapping
    public ResponseEntity<TargetAllocationResponse> createTargetAllocation(
            @Valid @NotNull @RequestBody TargetAllocationRequest request,
            @Parameter(hidden = true) Authentication authentication) {

        Integer userId = (Integer) authentication.getDetails();
        TargetAllocationResponse response =
                targetAllocationService.createTargetAllocation(userId, request);

        return ResponseEntity.status(201).body(response);
    }
}
