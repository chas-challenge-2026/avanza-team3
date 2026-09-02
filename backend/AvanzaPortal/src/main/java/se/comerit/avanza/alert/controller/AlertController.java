package se.comerit.avanza.alert.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.comerit.avanza.alert.dto.AlertResponse;
import se.comerit.avanza.alert.service.AlertService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<Page<AlertResponse>> getAlerts(
            @RequestParam(defaultValue = "false") boolean dismissed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        if (page < 0) {
            page = 0;
        }

        if (size < 1) {
            size = 20;
    }

        size = Math.min(size, 100);

        return ResponseEntity.ok(
                alertService.getAlertsByUserId(userId, dismissed, page, size)
        );
    }

    @GetMapping("/live")
    public ResponseEntity<List<Map<String, Object>>> getLiveAlerts(HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(alertService.getLiveAlertsByUserId(userId));
    }

    @PutMapping("/{alertId}/dismiss")
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
