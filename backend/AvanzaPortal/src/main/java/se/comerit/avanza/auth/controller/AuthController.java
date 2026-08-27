package se.comerit.avanza.auth.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import se.comerit.avanza.auth.service.AuthService;

@Controller
public class AuthController {

    private AuthService authService;

    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        // If already logged in, go home
        if (session.getAttribute("userId") != null) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        List<Map<String, Object>> rows = authService.authenticate(email, password, model);

        if (rows.isEmpty()) {
            model.addAttribute("error", "Fel e-post eller lösenord.");
            return "login";
        }

        Map<String, Object> user = rows.get(0);
        Integer userId = (Integer) user.get("id");
        String userName = (String) user.get("name");

        // Store user info in session
        session.setAttribute("userId", userId);
        session.setAttribute("userName", userName);
        session.setAttribute("userEmail", email);
        // tenantId is just userId for now, multi-tenant is future work
        session.setAttribute("tenantId", userId);

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

}
