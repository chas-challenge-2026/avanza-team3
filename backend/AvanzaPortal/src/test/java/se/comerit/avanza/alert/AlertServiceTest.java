package se.comerit.avanza.alert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import se.comerit.avanza.alert.dto.AlertResponse;
import se.comerit.avanza.alert.model.Alert;
import se.comerit.avanza.alert.repository.AlertRepository;
import se.comerit.avanza.alert.service.AlertService;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.portfolio.dto.PortfolioAllocationResult;
import se.comerit.avanza.portfolio.dto.PortfolioAllocationRow;
import se.comerit.avanza.portfolio.service.PortfolioAllocationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private PortfolioAllocationService portfolioAllocationService;

    @InjectMocks
    private AlertService alertService;

    @Test
    void getAlertsByUserIdPagedShouldReturnMappedPage() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 12, 9, 0);
        Alert alert = new Alert(7, "DRIFT", "Rebalance", false, createdAt);
        ReflectionTestUtils.setField(alert, "id", 42);

        Page<Alert> repositoryPage = new PageImpl<>(
                List.of(alert),
                PageRequest.of(1, 2),
                3
        );

        when(alertRepository.findByUserIdAndDismissedOrderByCreatedAtDesc(
                eq(7),
                eq(false),
                any(Pageable.class)
        )).thenReturn(repositoryPage);

        Page<AlertResponse> result = alertService.getAlertsByUserId(7, false, 1, 2);

        assertEquals(1, result.getContent().size());
        assertEquals(42, result.getContent().getFirst().id());
        assertEquals("DRIFT", result.getContent().getFirst().alertType());
        assertEquals("Rebalance", result.getContent().getFirst().message());
        assertFalse(result.getContent().getFirst().dismissed());
        assertEquals(createdAt, result.getContent().getFirst().createdAt());
        assertEquals(1, result.getNumber());
        assertEquals(2, result.getSize());
        assertEquals(3, result.getTotalElements());

        verify(alertRepository).findByUserIdAndDismissedOrderByCreatedAtDesc(
                eq(7),
                eq(false),
                eq(PageRequest.of(1, 2))
        );
    }

    @Test
    void getAlertsByUserIdShouldMapEntitiesToAlertResponses() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 1, 10, 0);
        Alert alert = new Alert(7, "DRIFT", "Rebalance", false, createdAt);
        ReflectionTestUtils.setField(alert, "id", 42);

        when(alertRepository.findByUserIdOrderByCreatedAtDesc(7)).thenReturn(List.of(alert));

        List<AlertResponse> result = alertService.getAlertsByUserId(7);

        assertEquals(1, result.size());
        AlertResponse response = result.getFirst();
        assertEquals(42, response.id());
        assertEquals("DRIFT", response.alertType());
        assertEquals("Rebalance", response.message());
        assertFalse(response.dismissed());
        assertEquals(createdAt, response.createdAt());
    }

    @Test
    void dismissAlertShouldDismissAlertOwnedByUser() {
        Alert alert = new Alert(7, "DRIFT", "Rebalance", false, LocalDateTime.now());
        when(alertRepository.findByIdAndUserId(42, 7)).thenReturn(Optional.of(alert));

        alertService.dismissAlert(42, 7);

        assertTrue(alert.isDismissed());
        verify(alertRepository).findByIdAndUserId(42, 7);
    }

    @Test
    void dismissAlertShouldRejectAlertNotOwnedByUser() {
        when(alertRepository.findByIdAndUserId(42, 7)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> alertService.dismissAlert(42, 7)
        );

        assertEquals("Alert not found", exception.getMessage());
    }

    @Test
    void getLiveAlertsShouldUseSharedAllocationResult() {
        when(portfolioAllocationService.getAccountTypeAllocation(7)).thenReturn(
                new PortfolioAllocationResult(
                        List.of(
                                row("ISK", "62.00", "50.00", "12.00", true),
                                row("KF", "38.00", "50.00", "12.00", true),
                                row("Pension", "0.00", "0.00", "0.00", false)
                        ),
                        new BigDecimal("100000.00"),
                        true
                )
        );

        List<Map<String, Object>> result = alertService.getLiveAlertsByUserId(7);

        assertEquals(2, result.size());
        assertEquals("LIVE_DRIFT", result.getFirst().get("alert_type"));
        assertTrue(result.getFirst().get("message").toString().contains("ISK-allokering"));
        assertTrue(result.getFirst().get("message").toString().contains("12,0")
                || result.getFirst().get("message").toString().contains("12.0"));
        assertEquals(false, result.getFirst().get("dismissed"));

        verify(portfolioAllocationService).getAccountTypeAllocation(7);
    }

    @Test
    void getLiveAlertsShouldSupportPensionWithoutSeparateAlertLogic() {
        when(portfolioAllocationService.getAccountTypeAllocation(7)).thenReturn(
                new PortfolioAllocationResult(
                        List.of(row("Pension", "20.00", "10.00", "10.00", true)),
                        new BigDecimal("50000.00"),
                        true
                )
        );

        List<Map<String, Object>> result = alertService.getLiveAlertsByUserId(7);

        assertEquals(1, result.size());
        assertTrue(result.getFirst().get("message").toString().contains("Pension-allokering"));
    }

    @Test
    void getLiveAlertsShouldReturnEmptyWhenSharedAllocationHasNoDrift() {
        when(portfolioAllocationService.getAccountTypeAllocation(7)).thenReturn(
                new PortfolioAllocationResult(
                        List.of(row("ISK", "100.00", "100.00", "0.00", false)),
                        new BigDecimal("1000.00"),
                        false
                )
        );

        List<Map<String, Object>> result = alertService.getLiveAlertsByUserId(7);

        assertTrue(result.isEmpty());
    }

    @Test
    void getDriftThresholdPercentShouldUsePortfolioAllocationThreshold() {
        when(portfolioAllocationService.getDriftThresholdPercent()).thenReturn(5);

        assertEquals(5, alertService.getDriftThresholdPercent());
    }

    private PortfolioAllocationRow row(
            String accountType,
            String actual,
            String target,
            String drift,
            boolean overThreshold) {
        return new PortfolioAllocationRow(
                accountType,
                new BigDecimal(actual),
                new BigDecimal(target),
                new BigDecimal(drift),
                overThreshold
        );
    }
}
