package se.comerit.avanza.account.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.comerit.avanza.account.dto.AccountPatchRequest;
import se.comerit.avanza.account.dto.AccountRequest;
import se.comerit.avanza.account.dto.AccountResponse;
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.account.repository.AccountRepository;
import se.comerit.avanza.exception.BusinessRuleException;
import se.comerit.avanza.exception.ResourceConflictException;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.holding.repository.HoldingRepository;

import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;

    public AccountService(AccountRepository accountRepository, HoldingRepository holdingRepository) {
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAccountsByUserId(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accountRepository.findByUserIdOrderByAccountTypeAscAccountNameAsc(userId, pageable)
                .map(this::toAccountResponse);
    }

    //lämna denna, behövs i backend utan pagination för att nå alla användarens konton, men låt inte frontend nå den. Finns version med pagination.
    @PreAuthorize("#userId == authentication.details")
    @Transactional(readOnly = true)
    public List<Account> getAccountsByUserId(Integer userId) {
        return accountRepository.findByUserIdOrderByAccountTypeAscAccountNameAsc(userId);
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional(readOnly = true)
    public Account getAccountByIdAndUserId(Integer accountId, Integer userId) {
        return findOwnedAccount(accountId, userId);
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional(readOnly = true)
    public AccountResponse getAccountResponse(Integer accountId, Integer userId) {
        return toAccountResponse(findOwnedAccount(accountId, userId));
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    public AccountResponse createAccount(Integer userId, AccountRequest request) {
        Account account = new Account(
                userId,
                normalizeRequired(request.accountType(), "Account type", 10),
                normalizeRequired(request.accountName(), "Account name", 100),
                normalizeCurrency(request.currency(), true));
        return toAccountResponse(accountRepository.save(account));
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    public AccountResponse replaceAccount(Integer accountId, Integer userId, AccountRequest request) {
        Account account = findOwnedAccount(accountId, userId);
        account.setAccountType(normalizeRequired(request.accountType(), "Account type", 10));
        account.setAccountName(normalizeRequired(request.accountName(), "Account name", 100));
        account.setCurrency(normalizeCurrency(request.currency(), true));
        return toAccountResponse(accountRepository.save(account));
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    public AccountResponse updateAccount(Integer accountId, Integer userId, AccountPatchRequest request) {
        Account account = findOwnedAccount(accountId, userId);
        if (request.accountType() == null && request.accountName() == null && request.currency() == null) {
            throw new BusinessRuleException("No changes supplied");
        }
        if (request.accountType() != null) {
            account.setAccountType(normalizeRequired(request.accountType(), "Account type", 10));
        }
        if (request.accountName() != null) {
            account.setAccountName(normalizeRequired(request.accountName(), "Account name", 100));
        }
        if (request.currency() != null) {
            account.setCurrency(normalizeCurrency(request.currency(), false));
        }
        return toAccountResponse(accountRepository.save(account));
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    public void deleteAccount(Integer accountId, Integer userId) {
        // Ingen delete om holdings finns kvar i account.
        Account account = accountRepository.findOwnedAccountForUpdate(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (holdingRepository.existsByAccountId(accountId)) {
            throw new ResourceConflictException("Account still has holdings");
        }
        accountRepository.delete(account);
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional(readOnly = true)
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

    private Account findOwnedAccount(Integer accountId, Integer userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private String normalizeRequired(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(field + " is required");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new BusinessRuleException(field + " is too long");
        }
        return trimmed;
    }

    private String normalizeCurrency(String value, boolean useDefaultWhenMissing) {
        if (value == null && useDefaultWhenMissing) {
            return "SEK";
        }
        if (value == null || !value.matches("[A-Za-z]{3}")) {
            throw new BusinessRuleException("Invalid account currency");
        }
        String currency = value.toUpperCase(Locale.ROOT);
        try {
            Currency.getInstance(currency);
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException("Invalid account currency");
        }
        return currency;
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
