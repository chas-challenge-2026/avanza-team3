package se.comerit.avanza.accountallocation;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import se.comerit.avanza.accountallocation.controller.AccountAllocationTargetController;
import se.comerit.avanza.accountallocation.dto.AccountAllocationTargetResponse;
import se.comerit.avanza.accountallocation.dto.AccountAllocationTargetsResponse;
import se.comerit.avanza.accountallocation.dto.ReplaceAccountTargetsRequest;
import se.comerit.avanza.accountallocation.dto.TargetItemRequest;
import se.comerit.avanza.accountallocation.model.AssetCategory;
import se.comerit.avanza.accountallocation.service.AccountAllocationTargetService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountAllocationTargetControllerTest {
    @Mock AccountAllocationTargetService service;
    @Mock Authentication authentication;
    @InjectMocks AccountAllocationTargetController controller;

    @BeforeEach
    void setUp() { lenient().when(authentication.getDetails()).thenReturn(7); }

    @Test
    void getUsesAuthenticatedUserIdentity() {
        var response = new AccountAllocationTargetsResponse(11, List.of());
        when(service.getTargets(11, 7)).thenReturn(response);
        assertSame(response, controller.getTargets(11, authentication).getBody());
    }

    @Test
    void putUsesAuthenticatedUserIdentity() {
        var request = new ReplaceAccountTargetsRequest(List.of(
                new TargetItemRequest(AssetCategory.STOCK, new BigDecimal("100"))));
        var response = new AccountAllocationTargetsResponse(11,
                List.of(new AccountAllocationTargetResponse(AssetCategory.STOCK, new BigDecimal("100"))));
        when(service.replaceTargets(11, 7, request)).thenReturn(response);
        assertSame(response, controller.replaceTargets(11, request, authentication).getBody());
    }

    @Test
    void deleteUsesAuthenticatedUserIdentityAndReturns204() {
        assertEquals(204, controller.deleteTargets(11, authentication).getStatusCode().value());
        verify(service).deleteTargets(11, 7);
    }
}
