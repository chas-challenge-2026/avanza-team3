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
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import se.comerit.avanza.account.dto.AccountResponse;
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.account.repository.AccountRepository;
import se.comerit.avanza.account.service.AccountService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

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

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> accountService.getAccountByIdAndUserId(11, 7)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found", exception.getReason());
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

