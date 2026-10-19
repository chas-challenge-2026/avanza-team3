package se.comerit.avanza.alert.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.comerit.avanza.alert.dto.AlertResponse;
import se.comerit.avanza.alert.model.Alert;
import se.comerit.avanza.alert.repository.AlertRepository;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.portfolio.dto.PortfolioAllocationResult;
import se.comerit.avanza.portfolio.dto.PortfolioAllocationRow;
import se.comerit.avanza.portfolio.service.PortfolioAllocationService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final PortfolioAllocationService portfolioAllocationService;

    public AlertService(
            AlertRepository alertRepository,
            PortfolioAllocationService portfolioAllocationService) {
        this.alertRepository = alertRepository;
        this.portfolioAllocationService = portfolioAllocationService;
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    public List<AlertResponse> getAlertsByUserId(Integer userId) {
        return alertRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toAlertResponse)
                .toList();
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    public Page<AlertResponse> getAlertsByUserId(
            Integer userId,
            boolean dismissed,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        return alertRepository
                .findByUserIdAndDismissedOrderByCreatedAtDesc(
                        userId,
                        dismissed,
                        pageable
                )
                .map(this::toAlertResponse);
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    public void dismissAlert(Integer alertId, Integer userId) {
        Alert alert = alertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        alert.setDismissed(true);
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLiveAlertsByUserId(Integer userId) {
        PortfolioAllocationResult allocation =
                portfolioAllocationService.getAccountTypeAllocation(userId);

        List<Map<String, Object>> liveAlerts = new ArrayList<>();

        for (PortfolioAllocationRow row : allocation.rows()) {
            if (!row.overThreshold()) {
                continue;
            }

            Map<String, Object> liveAlert = new LinkedHashMap<>();
            liveAlert.put("alert_type", "LIVE_DRIFT");
            liveAlert.put(
                    "message",
                    String.format(
                            "%s-allokering: faktisk %.1f%% vs mål %.1f%% (avvikelse %.1f procentenheter) — ombalansering rekommenderas",
                            row.accountType(),
                            row.actualPercentage().doubleValue(),
                            row.targetPercentage().doubleValue(),
                            row.driftPercentagePoints().doubleValue()
                    )
            );
            liveAlert.put("dismissed", false);
            liveAlert.put("created_at", "Nu");
            liveAlerts.add(liveAlert);
        }

        return liveAlerts;
    }

    private AlertResponse toAlertResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getAlertType(),
                alert.getMessage(),
                alert.isDismissed(),
                alert.getCreatedAt()
        );
    }

    public int getDriftThresholdPercent() {
        return portfolioAllocationService.getDriftThresholdPercent();
    }
}
