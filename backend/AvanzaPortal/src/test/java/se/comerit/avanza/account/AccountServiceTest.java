package se.comerit.avanza.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import se.comerit.avanza.account.dto.AccountResponse;
import se.comerit.avanza.account.dto.AccountRequest;
import se.comerit.avanza.account.dto.AccountPatchRequest;
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.account.repository.AccountRepository;
import se.comerit.avanza.account.service.AccountService;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.exception.ResourceConflictException;
import se.comerit.avanza.exception.BusinessRuleException;
import se.comerit.avanza.holding.repository.HoldingRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void getAccountsByUserIdShouldMapEntitiesToResponsesAndUseRequestedPagination() {
        Account account = account(11, 7, "ISK", "Main ISK", "SEK");

        when(accountRepository.findByUserIdOrderByAccountTypeAscAccountNameAsc(eq(7), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(account), invocation.getArgument(1), 1));

        Page<AccountResponse> result = accountService.getAccountsByUserId(7, 2, 15);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(accountRepository)
                .findByUserIdOrderByAccountTypeAscAccountNameAsc(eq(7), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(2, pageable.getPageNumber());
        assertEquals(15, pageable.getPageSize());

        assertEquals(1, result.getContent().size());
        AccountResponse response = result.getContent().getFirst();
        assertEquals(11, response.id());
        assertEquals("ISK", response.accountType());
        assertEquals("Main ISK", response.accountName());
        assertEquals("SEK", response.currency());
    }

    @Test
    void getAccountByIdAndUserIdShouldReturnAccountOwnedByUser() {
        Account account = account(11, 7, "ISK", "Main ISK", "SEK");
        when(accountRepository.findByIdAndUserId(11, 7)).thenReturn(Optional.of(account));

        Account result = accountService.getAccountByIdAndUserId(11, 7);

        assertSame(account, result);
        verify(accountRepository).findByIdAndUserId(11, 7);
    }

    @Test
    void getAccountByIdAndUserIdShouldRejectAccountNotOwnedByUser() {
        when(accountRepository.findByIdAndUserId(11, 7)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.getAccountByIdAndUserId(11, 7)
        );

        assertEquals("Account not found", exception.getMessage());
    }

    @Test
    void getAccountMapsByUserIdShouldExposeFieldsUsedByBackend() {
        Account account = account(11, 7, "ISK", "Main ISK", "SEK");
        when(accountRepository.findByUserIdOrderByAccountTypeAscAccountNameAsc(7))
                .thenReturn(List.of(account));

        List<Map<String, Object>> result = accountService.getAccountMapsByUserId(7);

        assertEquals(1, result.size());
        Map<String, Object> row = result.getFirst();
        assertEquals(11, row.get("id"));
        assertEquals(7, row.get("user_id"));
        assertEquals("ISK", row.get("account_type"));
        assertEquals("Main ISK", row.get("account_name"));
        assertEquals("SEK", row.get("currency"));
    }


    @Test
    void createAccountShouldTakeUserIdFromAuthenticationAndAllowMultipleIskAccounts() {
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse first = accountService.createAccount(7,
                new AccountRequest(" ISK ", " Pension ISK ", null));
        AccountResponse second = accountService.createAccount(7,
                new AccountRequest("ISK", "Semesterkonto", "sek"));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(2)).save(captor.capture());
        List<Account> accounts = captor.getAllValues();
        assertEquals(7, accounts.get(0).getUserId());
        assertEquals(7, accounts.get(1).getUserId());
        assertEquals("ISK", first.accountType());
        assertEquals("Pension ISK", first.accountName());
        assertEquals("SEK", first.currency());
        assertEquals("Semesterkonto", second.accountName());
        assertEquals("SEK", second.currency());
    }

    @Test
    void getAccountResponseShouldNeverExposeAnotherUsersAccount() {
        when(accountRepository.findByIdAndUserId(21, 7)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountResponse(21, 7));
        verify(accountRepository, never()).findById(21);
    }

    @Test
    void replaceShouldUpdateAllEditableFieldsWithoutChangingUserId() {
        Account original = account(11, 7, "ISK", "Original", "SEK");
        when(accountRepository.findByIdAndUserId(11, 7)).thenReturn(Optional.of(original));
        when(accountRepository.save(original)).thenReturn(original);

        AccountResponse result = accountService.replaceAccount(11, 7,
                new AccountRequest("KF", "Uppdaterat", "usd"));

        assertEquals(7, original.getUserId());
        assertEquals("KF", result.accountType());
        assertEquals("Uppdaterat", result.accountName());
        assertEquals("USD", result.currency());
    }

    @Test
    void patchShouldOnlyChangeProvidedFields() {
        Account original = account(11, 7, "ISK", "Original", "SEK");
        when(accountRepository.findByIdAndUserId(11, 7)).thenReturn(Optional.of(original));
        when(accountRepository.save(original)).thenReturn(original);

        AccountResponse result = accountService.updateAccount(11, 7,
                new AccountPatchRequest(null, "Ny etikett", null));
        assertEquals("ISK", result.accountType());
        assertEquals("Ny etikett", result.accountName());
        assertEquals("SEK", result.currency());
    }

    @Test
    void patchShouldRejectNoChangesEvenIfValidationWasBypassed() {
        when(accountRepository.findByIdAndUserId(11, 7))
                .thenReturn(Optional.of(account(11, 7, "ISK", "Original", "SEK")));
        assertThrows(BusinessRuleException.class,
                () -> accountService.updateAccount(11, 7, new AccountPatchRequest(null, null, null)));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void createShouldRejectInvalidCurrencyEvenIfValidationWasBypassed() {
        assertThrows(BusinessRuleException.class,
                () -> accountService.createAccount(7, new AccountRequest("ISK", "Test", "XYZ")));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void replaceAndPatchShouldRejectOtherUsersAccount() {
        when(accountRepository.findByIdAndUserId(11, 7)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> accountService.replaceAccount(11, 7, new AccountRequest("ISK", "X", "SEK")));
        assertThrows(ResourceNotFoundException.class,
                () -> accountService.updateAccount(11, 7, new AccountPatchRequest(null, "X", null)));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void deleteShouldRefuseAccountWithHoldings() {
        Account account = account(11, 7, "ISK", "Main ISK", "SEK");
        when(accountRepository.findOwnedAccountForUpdate(11, 7)).thenReturn(Optional.of(account));
        when(holdingRepository.existsByAccountId(11)).thenReturn(true);
        assertThrows(ResourceConflictException.class, () -> accountService.deleteAccount(11, 7));
        verify(accountRepository, never()).delete(any());
    }

    @Test
    void deleteShouldRemoveEmptyOwnedAccount() {
        Account account = account(11, 7, "ISK", "Main ISK", "SEK");
        when(accountRepository.findOwnedAccountForUpdate(11, 7)).thenReturn(Optional.of(account));
        when(holdingRepository.existsByAccountId(11)).thenReturn(false);
        accountService.deleteAccount(11, 7);
        verify(accountRepository).delete(account);
    }

    @Test
    void deleteShouldReturnNotFoundForAnotherUsersAccount() {
        when(accountRepository.findOwnedAccountForUpdate(11, 7)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> accountService.deleteAccount(11, 7));
        verifyNoInteractions(holdingRepository);
        verify(accountRepository, never()).delete(any());
    }

    private Account account(
            Integer id,
            Integer userId,
            String accountType,
            String accountName,
            String currency) {

        Account account = new Account(userId, accountType, accountName, currency);
        ReflectionTestUtils.setField(account, "id", id);
        return account;
    }
}

