package se.comerit.avanza.account.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import se.comerit.avanza.account.dto.AccountResponse;
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.account.repository.AccountRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Page<AccountResponse> getAccountsByUserId(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accountRepository.findByUserIdOrderByAccountTypeAscAccountNameAsc(userId, pageable)
                .map(this::toAccountResponse);
    }

    //lämna denna, behövs i backend utan pagination för att nå alla användarens konton, men låt inte frontend nå den. Finns version med pagination.
    @Transactional
    public List<Account> getAccountsByUserId(Integer userId) {
        return accountRepository.findByUserIdOrderByAccountTypeAscAccountNameAsc(userId);
    }

    @Transactional
    public Account getAccountByIdAndUserId(Integer accountId, Integer userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    @Transactional
    public List<Map<String, Object>> getAccountMapsByUserId(Integer userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Account account : getAccountsByUserId(userId)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", account.getId());
            row.put("user_id", account.getUserId());
            row.put("account_type", account.getAccountType());
            row.put("account_name", account.getAccountName());
            row.put("currency", account.getCurrency());
            result.add(row);
        }
        return result;
    }

    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountType(),
                account.getAccountName(),
                account.getCurrency()
        );
    }
}
