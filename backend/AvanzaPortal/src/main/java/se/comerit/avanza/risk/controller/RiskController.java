package se.comerit.avanza.risk.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.comerit.avanza.risk.PortfolioRiskService;
import se.comerit.avanza.risk.dto.PortfolioRiskResponse;

@RestController
@RequestMapping("/api/risk")
@Tag(
        name = "Risk",
        description = "Endpoints for portfolio risk metrics and risk history"
)
@SecurityRequirement(name = "bearerAuth")
public class RiskController {

    private final PortfolioRiskService portfolioRiskService;

    public RiskController(PortfolioRiskService portfolioRiskService) {
        this.portfolioRiskService = portfolioRiskService;
    }

    @Operation(
            summary = "Get portfolio risk metrics",
            description = "Returns current portfolio risk metrics and at least three days of historical risk metrics for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Risk metrics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/portfolio")
    public ResponseEntity<PortfolioRiskResponse> getPortfolioRisk(
            @Parameter(hidden = true)
            Authentication authentication) {
        Integer userId = authenticatedUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(portfolioRiskService.getPortfolioRisk(userId));
    }

    private Integer authenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object details = authentication.getDetails();
        if (details instanceof Integer userId) {
            return userId;
        }

        try {
            return Integer.valueOf(authentication.getName());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
