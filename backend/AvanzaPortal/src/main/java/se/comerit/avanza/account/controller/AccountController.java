package se.comerit.avanza.account.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.comerit.avanza.account.dto.AccountResponse;
import se.comerit.avanza.account.service.AccountService;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<Page<AccountResponse>> getAccounts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, HttpSession httpSession) {
        Integer userId = (Integer) httpSession.getAttribute("userId");
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
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId, page, size));
    }
}
