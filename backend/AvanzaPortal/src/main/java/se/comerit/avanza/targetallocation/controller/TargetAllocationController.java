package se.comerit.avanza.targetallocation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.comerit.avanza.targetallocation.dto.TargetAllocationResponse;
import se.comerit.avanza.targetallocation.service.TargetAllocationService;

@RestController
@RequestMapping("/api/target-allocations")
@Tag(
        name = "Target Allocations",
        description = "Endpoints for viewing the authenticated user's target allocations"
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
}
