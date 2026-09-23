package se.comerit.avanza.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.comerit.avanza.account.controller.AccountController;
import se.comerit.avanza.account.service.AccountService;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
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
