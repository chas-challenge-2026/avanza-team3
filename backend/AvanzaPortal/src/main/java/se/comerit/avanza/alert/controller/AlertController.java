package se.comerit.avanza.alert.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import se.comerit.avanza.alert.dto.AlertResponse;
import se.comerit.avanza.alert.service.AlertService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@Tag(
        name = "Alerts",
        description = "Endpoints for stored and live portfolio alerts"
)
@SecurityRequirement(name = "bearerAuth")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @Operation(
            summary = "Get stored alerts",
            description = "Returns a paginated list of stored alerts belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    public ResponseEntity<Page<AlertResponse>> getAlerts(
            @Parameter(
                    description = "Whether dismissed alerts should be returned",
                    example = "false"
            )
            @RequestParam(defaultValue = "false") boolean dismissed,

            @Parameter(
                    description = "Page number. Page numbering starts at 0.",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Number of alerts per page. Default is 20 and maximum is 100.",
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

        return ResponseEntity.ok(alertService.getAlertsByUserId(userId, dismissed, page, size));
    }

    @Operation(
            summary = "Get live alerts",
            description = "Calculates current portfolio allocation alerts for the authenticated user. Live alerts are calculated on request and are not stored."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Live alerts calculated successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/live")
    public ResponseEntity<List<Map<String, Object>>> getLiveAlerts(
            @Parameter(hidden = true)
            Authentication authentication) {

        Integer userId = (Integer) authentication.getDetails();
        return ResponseEntity.ok(alertService.getLiveAlertsByUserId(userId));
    }

    @Operation(
            summary = "Dismiss alert",
            description = "Marks a stored alert belonging to the authenticated user as dismissed."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Alert dismissed successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PatchMapping("/{alertId}/dismiss")
    public ResponseEntity<Void> dismissAlert(
            @Parameter(description = "ID of the alert to dismiss", example = "42")
            @PathVariable Integer alertId,

            @Parameter(hidden = true)
            Authentication authentication) {

        Integer userId = (Integer) authentication.getDetails();
        alertService.dismissAlert(alertId, userId);

        return ResponseEntity.noContent().build();
    }
}
