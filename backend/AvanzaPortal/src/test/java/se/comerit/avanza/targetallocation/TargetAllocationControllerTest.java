package se.comerit.avanza.targetallocation;

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
import se.comerit.avanza.targetallocation.controller.TargetAllocationController;
import se.comerit.avanza.targetallocation.service.TargetAllocationService;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TargetAllocationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TargetAllocationService targetAllocationService;

    @InjectMocks
    private TargetAllocationController targetAllocationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(targetAllocationController).build();
    }

    @Test
    void getTargetAllocationsShouldUseAuthenticatedUserAndDefaultPagination() throws Exception {
        when(targetAllocationService.getTargetAllocationsByUserId(7, 0, 20))
                .thenReturn(Page.empty(PageRequest.of(0, 20)));

        mockMvc.perform(get("/api/target-allocations").principal(authenticationForUser(7)))
                .andExpect(status().isOk());

        verify(targetAllocationService).getTargetAllocationsByUserId(7, 0, 20);
    }

    @Test
    void getTargetAllocationsShouldNormalizeInvalidPaginationAndCapPageSize() throws Exception {
        when(targetAllocationService.getTargetAllocationsByUserId(7, 0, 100))
                .thenReturn(Page.empty(PageRequest.of(0, 100)));

        mockMvc.perform(get("/api/target-allocations")
                        .param("page", "-2")
                        .param("size", "250")
                        .principal(authenticationForUser(7)))
                .andExpect(status().isOk());

        verify(targetAllocationService).getTargetAllocationsByUserId(7, 0, 100);
    }

    @Test
    void getTargetAllocationsShouldResetNonPositivePageSizeToDefault() throws Exception {
        when(targetAllocationService.getTargetAllocationsByUserId(7, 1, 20))
                .thenReturn(Page.empty(PageRequest.of(1, 20)));

        mockMvc.perform(get("/api/target-allocations")
                        .param("page", "1")
                        .param("size", "0")
                        .principal(authenticationForUser(7)))
                .andExpect(status().isOk());

        verify(targetAllocationService).getTargetAllocationsByUserId(7, 1, 20);
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
