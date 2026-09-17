package se.comerit.avanza.holding.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Holdings",
        description = "Endpoints for viewing and managing the authenticated user's holdings"
)
@SecurityRequirement(name = "bearerAuth")
public class HoldingController {

    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    @Operation(
            summary = "Get holdings",
            description = "Returns a paginated list of holdings belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Holdings retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @GetMapping
    public ResponseEntity<Page<Map<String, Object>>> listHoldings(

            @Parameter(
                    description = "Page number. Page numbering starts at 0.",
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(
                    description = "Number of holdings per page. Default is 20 and maximum is 100.",
                    example = "20"
            )
            @RequestParam(defaultValue = "20")
            int size,

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

        Page<Map<String, Object>> holdings =
                holdingService.getHoldingsByUserId(userId, page, size);

        return ResponseEntity.ok(holdings);
    }


    @Operation(
            summary = "Create holding",
            description = "Creates a new holding in an account belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Holding created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid holding data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not authorized to create a holding in the specified account"
            )
    })
    @PostMapping
    public ResponseEntity<Void> addHolding(

            @Valid
            @RequestBody
            HoldingRequest request,

            @Parameter(hidden = true)
            Authentication authentication) {

        Integer userId = (Integer) authentication.getDetails();

        holdingService.addHolding(
                userId,
                request.accountId(),
                request.ticker(),
                request.instrumentName(),
                request.quantity(),
                request.avgBuyPrice(),
                request.currency()
        );

        return ResponseEntity.status(201).build();
    }


    @Operation(
            summary = "Update holding",
            description = "Partially updates a holding belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Holding updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid holding data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not authorized to update this holding"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Holding not found"
            )
    })
    @PatchMapping("/{holdingId}")
    public ResponseEntity<HoldingResponse> updateHolding(

            @Parameter(
                    description = "ID of the holding to update",
                    example = "1"
            )
            @PathVariable("holdingId")
            Integer holdingId,

            @Valid
            @RequestBody
            HoldingPatchRequest request,

            @Parameter(hidden = true)
            Authentication authentication) {

        Integer userId = (Integer) authentication.getDetails();

        return ResponseEntity.ok(
                holdingService.updateHolding(
                        holdingId,
                        userId,
                        request
                )
        );
    }


    @Operation(
            summary = "Get holding by ID",
            description = "Returns a holding by ID if it belongs to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Holding retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not authorized to access this holding"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Holding not found"
            )
    })
    @GetMapping("/{holdingId}")
    public ResponseEntity<HoldingResponse> getHolding(

            @Parameter(
                    description = "ID of the holding",
                    example = "1"
            )
            @PathVariable("holdingId")
            Integer holdingId,

            @Parameter(hidden = true)
            Authentication authentication) {

        Integer userId = (Integer) authentication.getDetails();

        return ResponseEntity.ok(
                holdingService.getHoldingById(
                        holdingId,
                        userId
                )
        );
    }


    @Operation(
            summary = "Delete holding",
            description = "Deletes a holding belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Holding deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not authorized to delete this holding"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Holding not found"
            )
    })
    @DeleteMapping("/{holdingId}")
    public ResponseEntity<Void> deleteHolding(

            @Parameter(
                    description = "ID of the holding to delete",
                    example = "1"
            )
            @PathVariable("holdingId")
            Integer holdingId,

            @Parameter(hidden = true)
            Authentication authentication) {

        Integer userId = (Integer) authentication.getDetails();

        holdingService.deleteHolding(
                holdingId,
                userId
        );

        return ResponseEntity.noContent().build();
    }
}