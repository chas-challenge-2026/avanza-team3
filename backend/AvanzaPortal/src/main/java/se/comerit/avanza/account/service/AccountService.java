package se.comerit.avanza.account.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.account.repository.AccountRepository;

import java.util.List;

public class AccountService {

    private AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAccountsByUserId(Integer userId) {
        return accountRepository.findByUserIdOrderByAccountTypeAscAccountNameAsc(userId);
    }

    public Account getAccountByIdForUser(Integer accountId, Integer userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }


}
