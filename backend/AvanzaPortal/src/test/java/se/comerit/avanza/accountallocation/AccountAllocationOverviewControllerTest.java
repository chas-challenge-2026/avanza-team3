package se.comerit.avanza.accountallocation;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import se.comerit.avanza.accountallocation.controller.AccountAllocationOverviewController;
import se.comerit.avanza.accountallocation.dto.AccountAllocationOverviewResponse;
import se.comerit.avanza.accountallocation.service.AccountAllocationOverviewService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountAllocationOverviewControllerTest {
    @Mock AccountAllocationOverviewService service;
    @Mock Authentication authentication;
    @InjectMocks AccountAllocationOverviewController controller;

    @Test
    void getAllocationUsesUserIdFromJwtAuthenticationDetails() {
        when(authentication.getDetails()).thenReturn(7);
        var expected = new AccountAllocationOverviewResponse(11,
                new BigDecimal("0.00"), false, List.of());
        when(service.getAllocation(11, 7)).thenReturn(expected);
        var result = controller.getAllocation(11, authentication);
        assertEquals(200, result.getStatusCode().value());
        assertSame(expected, result.getBody());
        verify(service).getAllocation(11, 7);
    }
}
