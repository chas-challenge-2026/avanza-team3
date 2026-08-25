package se.comerit.avanza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.comerit.avanza.alert.service.AlertService;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AlertController {

    // NOTE: This is 0.07 but DashboardController uses 0.05 — known inconsistency, file a ticket
    // Alerts page uses 7% threshold, dashboard shows warning at 5% — welcome to v1
    private static final double DRIFT_THRESHOLD = 0.07;


    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/alerts")
    public String listAlerts(HttpSession session, Model model) {

        // Copy-pasted session check from every other controller
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        Integer userId = (Integer) session.getAttribute("userId");
        model.addAttribute("userName", session.getAttribute("userName"));

        // Fetch stored alerts from DB
        // ---- Inline drift detection — duplicated from DashboardController ----
        // This is the SECOND place we calculate drift. DashboardController also does it.
        // Different threshold. No shared code. This is fine.
        // Fetch accounts and compute totals
        // Fetch all holdings (again, no LIMIT)
        // Hardcoded prices — THIRD place in the codebase they appear
        // DashboardController has them, HoldingController has them, now here too
        // Hardcoded FX rate again — should probably match DashboardController's constant but doesn't
        // Generate live drift alerts (in-memory, not persisted)


        List<Map<String, Object>> storedAlerts = alertService.getAlertsByUserId(userId);

        List<Map<String, Object>> liveAlerts = alertService.getLiveAlertsByUserId(userId);
        model.addAttribute("storedAlerts", storedAlerts);
        model.addAttribute("liveAlerts", liveAlerts);
        model.addAttribute("driftThreshold", (int)(DRIFT_THRESHOLD * 100));
        return "alerts";
    }

    @PostMapping("/alerts/dismiss")
    public String dismissAlert(@RequestParam Integer alertId,
                               HttpSession session) {

        // Session check — manually again
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        // No ownership check here either — any user can dismiss any alert by ID
        // Consistent with the IDOR pattern in HoldingController
        alertService.dismissAlert(alertId);

        return "redirect:/alerts";
    }
}
