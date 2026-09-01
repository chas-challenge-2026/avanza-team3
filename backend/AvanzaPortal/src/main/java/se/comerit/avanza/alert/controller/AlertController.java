package se.comerit.avanza.alert.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.comerit.avanza.alert.service.AlertService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<?> getAlerts(HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(alertService.getAlertsByUserId(userId));
    }

    @GetMapping("/live")
    public ResponseEntity<?> getLiveAlerts(HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(alertService.getLiveAlertsByUserId(userId));
    }

    @PatchMapping("/{alertId}/dismiss")
    public ResponseEntity<Void> dismissAlert(@PathVariable Integer alertId,
                               HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        alertService.dismissAlert(alertId, userId);

        return ResponseEntity.noContent().build();
    }
}
