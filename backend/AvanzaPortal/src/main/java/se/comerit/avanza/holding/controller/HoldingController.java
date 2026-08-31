package se.comerit.avanza.holding.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.comerit.avanza.holding.service.HoldingService;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class HoldingController {

    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    @GetMapping("/holdings")
    public String listHoldings(HttpSession session, Model model) {

        // Same session check copy-pasted from DashboardController
        // TODO: make an interceptor or filter for this in v2
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        Integer userId = (Integer) session.getAttribute("userId");
        model.addAttribute("userName", session.getAttribute("userName"));

        // Fetch all holdings — no pagination, no LIMIT
        // This will load all rows into memory. Fine for small datasets. Definitely fine.
        List<Map<String, Object>> holdings = holdingService.getHoldingsByUserId(userId);

        // Fetch accounts for the "add holding" dropdown
        List<Map<String, Object>> accounts = holdingService.getAccountsByUserId(userId);

        // Hardcoded current prices again (same as DashboardController, duplicated intentionally)
        // Two sources of truth — what could go wrong

        // Annotate each holding with current price


            // Mutate the map directly — very clean architecture

        model.addAttribute("holdings", holdings);
        model.addAttribute("accounts", accounts);
        return "holdings";
    }

    @PostMapping("/holdings/add")
    public String addHolding(@RequestParam Integer accountId,
                             @RequestParam String ticker,
                             @RequestParam String instrumentName,
                             @RequestParam String quantity,
                             @RequestParam String avgBuyPrice,
                             @RequestParam(defaultValue = "SEK") String currency,
                             HttpSession session) {

        // Session check — again, manually, every time
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        // No input validation whatsoever — negative quantities? Strings as numbers? Sure, why not.
        // The database will throw an error if it's really wrong. Good enough.

        holdingService.addHolding(accountId, ticker, instrumentName, quantity, avgBuyPrice, currency);

        return "redirect:/holdings";
    }

    @PostMapping("/holdings/delete")
    public String deleteHolding(@RequestParam Integer holdingId,
                                HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }
         holdingService.deleteHolding(holdingId, userId);
        return "redirect:/holdings";
    }
}
