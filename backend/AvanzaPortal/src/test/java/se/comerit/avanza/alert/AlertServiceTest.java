package se.comerit.avanza.alert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.comerit.avanza.account.service.AccountService;
import se.comerit.avanza.alert.dto.AlertResponse;
import se.comerit.avanza.alert.model.Alert;
import se.comerit.avanza.alert.repository.AlertRepository;
import se.comerit.avanza.alert.service.AlertService;
import se.comerit.avanza.holding.service.HoldingService;
import se.comerit.avanza.targetallocation.service.TargetAllocationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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
    private HoldingService holdingService;

    @Mock
    private AccountService accountService;

    @Mock
    private TargetAllocationService targetAllocationService;

    @InjectMocks
    private AlertService alertService;

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

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> alertService.dismissAlert(42, 7)
        );

        assertEquals("Alert not found", exception.getMessage());
    }

    @Test
    void getLiveAlertsShouldCreateDriftAlertWhenAllocationExceedsThreshold() {
        when(accountService.getAccountMapsByUserId(7)).thenReturn(List.of(accountMap(11, "ISK")));
        when(holdingService.getHoldingsByUserId(7)).thenReturn(List.of(
                holdingMap(11, "ERIC-B", "SEK", new BigDecimal("10"))
        ));
        when(targetAllocationService.getTargetMapsByUserId(7)).thenReturn(List.of(
                targetMap("ISK", new BigDecimal("50.00"))
        ));

        List<Map<String, Object>> result = alertService.getLiveAlertsByUserId(7);

        assertEquals(1, result.size());
        assertEquals("LIVE_DRIFT", result.getFirst().get("alert_type"));
        assertTrue(result.getFirst().get("message").toString().contains("ISK-allokering"));
        assertEquals(false, result.getFirst().get("dismissed"));
    }

    @Test
    void getLiveAlertsShouldReturnEmptyWhenAllocationMatchesTarget() {
        when(accountService.getAccountMapsByUserId(7)).thenReturn(List.of(accountMap(11, "ISK")));
        when(holdingService.getHoldingsByUserId(7)).thenReturn(List.of(
                holdingMap(11, "ERIC-B", "SEK", new BigDecimal("10"))
        ));
        when(targetAllocationService.getTargetMapsByUserId(7)).thenReturn(List.of(
                targetMap("ISK", new BigDecimal("100.00"))
        ));

        List<Map<String, Object>> result = alertService.getLiveAlertsByUserId(7);

        assertTrue(result.isEmpty());
    }

    @Test
    void getDriftThresholdPercentShouldReturnSeven() {
        assertEquals(7, alertService.getDriftThresholdPercent());
    }

    private Map<String, Object> accountMap(Integer id, String accountType) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("account_type", accountType);
        return map;
    }

    private Map<String, Object> holdingMap(
            Integer accountId,
            String ticker,
            String currency,
            BigDecimal quantity) {

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("account_id", accountId);
        map.put("ticker", ticker);
        map.put("currency", currency);
        map.put("quantity", quantity);
        return map;
    }

    private Map<String, Object> targetMap(String accountType, BigDecimal targetPct) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("account_type", accountType);
        map.put("target_pct", targetPct);
        return map;
    }
}
