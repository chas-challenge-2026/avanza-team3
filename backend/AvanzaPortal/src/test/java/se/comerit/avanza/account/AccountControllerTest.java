package se.comerit.avanza.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import se.comerit.avanza.account.dto.AccountResponse;
import se.comerit.avanza.account.dto.AccountRequest;
import se.comerit.avanza.account.dto.AccountPatchRequest;
import se.comerit.avanza.exception.GlobalExceptionHandler;
import se.comerit.avanza.exception.ResourceConflictException;
import se.comerit.avanza.exception.ResourceNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.comerit.avanza.account.controller.AccountController;
import se.comerit.avanza.account.service.AccountService;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void getAccountsShouldUseAuthenticatedUserAndDefaultPagination() throws Exception {
        when(accountService.getAccountsByUserId(7, 0, 20))
                .thenReturn(Page.empty(PageRequest.of(0, 20)));

        mockMvc.perform(get("/api/accounts").principal(authenticationForUser(7)))
                .andExpect(status().isOk());

        verify(accountService).getAccountsByUserId(7, 0, 20);
    }

    @Test
    void getAccountsShouldNormalizeInvalidPaginationAndCapPageSize() throws Exception {
        when(accountService.getAccountsByUserId(7, 0, 100))
                .thenReturn(Page.empty(PageRequest.of(0, 100)));

        mockMvc.perform(get("/api/accounts")
                        .param("page", "-3")
                        .param("size", "500")
                        .principal(authenticationForUser(7)))
                .andExpect(status().isOk());

        verify(accountService).getAccountsByUserId(7, 0, 100);
    }

    @Test
    void getAccountsShouldResetNonPositivePageSizeToDefault() throws Exception {
        when(accountService.getAccountsByUserId(7, 1, 20))
                .thenReturn(Page.empty(PageRequest.of(1, 20)));

        mockMvc.perform(get("/api/accounts")
                        .param("page", "1")
                        .param("size", "0")
                        .principal(authenticationForUser(7)))
                .andExpect(status().isOk());

        verify(accountService).getAccountsByUserId(7, 1, 20);
    }


    @Test
    void getAccountShouldUseAuthenticatedUser() throws Exception {
        when(accountService.getAccountResponse(11, 7))
                .thenReturn(new AccountResponse(11, "ISK", "Main ISK", "SEK"));
        mockMvc.perform(get("/api/accounts/11").principal(authenticationForUser(7)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11));
        verify(accountService).getAccountResponse(11, 7);
    }

    @Test
    void postShouldCreateAccountAndReturnLocation() throws Exception {
        when(accountService.createAccount(eq(7), any(AccountRequest.class)))
                .thenReturn(new AccountResponse(27, "ISK", "Nytt konto", "SEK"));
        mockMvc.perform(post("/api/accounts")
                        .principal(authenticationForUser(7))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountType\":\"ISK\",\"accountName\":\"Nytt konto\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/accounts/27"))
                .andExpect(jsonPath("$.id").value(27));
        verify(accountService).createAccount(eq(7), any(AccountRequest.class));
    }

    @Test
    void putShouldReplaceAccountForAuthenticatedUser() throws Exception {
        when(accountService.replaceAccount(eq(11), eq(7), any(AccountRequest.class)))
                .thenReturn(new AccountResponse(11, "KF", "New name", "USD"));
        mockMvc.perform(put("/api/accounts/11")
                        .principal(authenticationForUser(7))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountType\":\"KF\",\"accountName\":\"New name\",\"currency\":\"USD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"));
        verify(accountService).replaceAccount(eq(11), eq(7), any(AccountRequest.class));
    }

    @Test
    void patchShouldUpdateAccountForAuthenticatedUser() throws Exception {
        when(accountService.updateAccount(eq(11), eq(7), any(AccountPatchRequest.class)))
                .thenReturn(new AccountResponse(11, "ISK", "Updated", "SEK"));
        mockMvc.perform(patch("/api/accounts/11")
                        .principal(authenticationForUser(7))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountName\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountName").value("Updated"));
        verify(accountService).updateAccount(eq(11), eq(7), any(AccountPatchRequest.class));
    }

    @Test
    void deleteShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/accounts/11").principal(authenticationForUser(7)))
                .andExpect(status().isNoContent());
        verify(accountService).deleteAccount(11, 7);
    }

    @Test
    void invalidCreateShouldReturn400WithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .principal(authenticationForUser(7))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountType\":\"ISK\",\"accountName\":\"  \"}"))
                .andExpect(status().isBadRequest());
        org.mockito.Mockito.verifyNoInteractions(accountService);
    }

    @Test
    void emptyPatchShouldReturn400WithoutCallingService() throws Exception {
        mockMvc.perform(patch("/api/accounts/11")
                        .principal(authenticationForUser(7))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        org.mockito.Mockito.verifyNoInteractions(accountService);
    }


    @Test
    void getAccountShouldReturn404ForMissingOrUnownedAccount() throws Exception {
        when(accountService.getAccountResponse(11, 7))
                .thenThrow(new ResourceNotFoundException("Account not found"));
        mockMvc.perform(get("/api/accounts/11").principal(authenticationForUser(7)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteWithHoldingsShouldReturn409() throws Exception {
        doThrow(new ResourceConflictException("Account still has holdings"))
                .when(accountService).deleteAccount(11, 7);
        mockMvc.perform(delete("/api/accounts/11").principal(authenticationForUser(7)))
                .andExpect(status().isConflict());
    }

    private UsernamePasswordAuthenticationToken authenticationForUser(Integer userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "test@example.com",
                        null,
                        Collections.emptyList());
        authentication.setDetails(userId);
        return authentication;
    }
}
