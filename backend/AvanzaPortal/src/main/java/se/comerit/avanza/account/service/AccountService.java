package se.comerit.avanza.account.service;

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
}
