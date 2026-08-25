package se.comerit.avanza.alert.service;

import org.springframework.stereotype.Service;
import se.comerit.avanza.alert.repository.AlertRepository;

import java.util.List;
import java.util.Map;

@Service
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public List<Map<String, Object>> getAccountsByUserId(Integer userId) {
        return alertRepository.getAccountsByUserId(userId);
    }

    public List<Map<String, Object>> getHoldingsForAlertByUserId(Integer userId) {
        return alertRepository.getHoldingsForAlertByUserId(userId);
    }

    public List<Map<String, Object>> getAlertsByUserId(Integer userId) {

        return alertRepository.getAlerts(userId);
    }

    public List<Map<String, Object>> getTargetByUserId(Integer userId) {
        return alertRepository.getTargetByUserId(userId);
    }

    public void dismissAlert(Integer alertId) {
        alertRepository.dismissAlert(alertId);
    }
}
