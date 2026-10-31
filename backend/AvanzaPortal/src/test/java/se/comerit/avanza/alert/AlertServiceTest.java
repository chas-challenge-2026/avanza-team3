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

import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.account.repository.AccountRepository;
import se.comerit.avanza.accountallocation.dto.AccountAllocationCategoryResponse;
import se.comerit.avanza.accountallocation.dto.AccountAllocationOverviewResponse;
import se.comerit.avanza.accountallocation.model.AssetCategory;
import se.comerit.avanza.accountallocation.service.AccountAllocationOverviewService;
import se.comerit.avanza.alert.dto.AlertResponse;
import se.comerit.avanza.alert.dto.LiveAlertDto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

        @Mock
        private AlertRepository alertRepository;

        @Mock
        private AccountRepository accountRepository;

        @Mock
        private AccountAllocationOverviewService accountAllocationOverviewService;

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
                                3);

                when(alertRepository.findByUserIdAndDismissedOrderByCreatedAtDesc(
                                eq(7),
                                eq(false),
                                any(Pageable.class))).thenReturn(repositoryPage);

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
                                eq(PageRequest.of(1, 2)));
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
                                () -> alertService.dismissAlert(42, 7));

                assertEquals("Alert not found", exception.getMessage());
        }

        @Test
        void getLiveAlertsShouldReturnStockDriftAlert() {
                Account account = createAccount(1, "ISK", "Mitt ISK");

                when(accountRepository.findByUserIdOrderByAccountTypeAscAccountNameAsc(7))
                                .thenReturn(List.of(account));

                when(accountAllocationOverviewService.getAllocation(1, 7))
                                .thenReturn(createAllocation(
                                                true,
                                                category(AssetCategory.STOCK, "75.00", "60.00", "15.00")));

                List<LiveAlertDto> result = alertService.getLiveAlertsByUserId(7);

                assertEquals(1, result.size());

                LiveAlertDto alert = result.getFirst();

                assertEquals("LIVE_DRIFT", alert.alertType());
                assertEquals(1, alert.accountId());
                assertEquals("Mitt ISK", alert.accountName());
                assertEquals("STOCK", alert.category());
                assertEquals(new BigDecimal("75.00"), alert.actualPct());
                assertEquals(new BigDecimal("60.00"), alert.targetPct());
                assertEquals(new BigDecimal("15.00"), alert.differencePctPoints());
                assertFalse(alert.dismissed());
                assertEquals("Nu", alert.createdAt());

                assertTrue(alert.message().contains("STOCK"));
                assertTrue(alert.message().contains("över"));
                assertTrue(alert.message().contains("Mitt ISK"));

                verify(accountRepository)
                                .findByUserIdOrderByAccountTypeAscAccountNameAsc(7);

                verify(accountAllocationOverviewService).getAllocation(1, 7);

        }

        @Test
        void getLiveAlertsShouldReturnFundDriftAlertBelowTarget() {
                Account account = createAccount(1, "ISK", "Mitt ISK");

                when(accountRepository.findByUserIdOrderByAccountTypeAscAccountNameAsc(7))
                                .thenReturn(List.of(account));

                when(accountAllocationOverviewService.getAllocation(1, 7))
                                .thenReturn(createAllocation(
                                                true,
                                                category(AssetCategory.FUND, "25.00", "40.00", "-15.00")));

                List<LiveAlertDto> result = alertService.getLiveAlertsByUserId(7);

                assertEquals(1, result.size());

                LiveAlertDto alert = result.getFirst();

                assertEquals("FUND", alert.category());
                assertEquals(new BigDecimal("-15.00"), alert.differencePctPoints());
                assertTrue(alert.message().contains("under"));
        }

        @Test
        void getLiveAlertsShouldReturnEmptyWhenDriftIsBelowThreshold() {
                Account account = createAccount(1, "ISK", "Mitt ISK");

                when(accountRepository.findByUserIdOrderByAccountTypeAscAccountNameAsc(7))
                                .thenReturn(List.of(account));

                when(accountAllocationOverviewService.getAllocation(1, 7))
                                .thenReturn(createAllocation(
                                                true,
                                                category(AssetCategory.STOCK, "63.00", "60.00", "3.00")));

                List<LiveAlertDto> result = alertService.getLiveAlertsByUserId(7);

                assertTrue(result.isEmpty());
        }

        @Test
        void getLiveAlertsShouldNotReturnAlertAtExactThreshold() {
                Account account = createAccount(1, "ISK", "Mitt ISK");

                when(accountRepository.findByUserIdOrderByAccountTypeAscAccountNameAsc(7))
                                .thenReturn(List.of(account));

                when(accountAllocationOverviewService.getAllocation(1, 7))
                                .thenReturn(createAllocation(
                                                true,
                                                category(AssetCategory.STOCK, "65.00", "60.00", "5.00")));

                List<LiveAlertDto> result = alertService.getLiveAlertsByUserId(7);

                assertTrue(result.isEmpty());
        }

        @Test
        void getLiveAlertsShouldReturnEmptyWhenTargetsAreNotConfigured() {
                Account account = createAccount(1, "ISK", "Mitt ISK");

                when(accountRepository.findByUserIdOrderByAccountTypeAscAccountNameAsc(7))
                                .thenReturn(List.of(account));

                when(accountAllocationOverviewService.getAllocation(1, 7))
                                .thenReturn(createAllocation(false));

                List<LiveAlertDto> result = alertService.getLiveAlertsByUserId(7);

                assertTrue(result.isEmpty());

                verify(accountAllocationOverviewService).getAllocation(1, 7);
        }

        @Test
        void getLiveAlertsShouldCheckAllAccountsSeparately(){
                Account isk = createAccount(1, "ISK", "Mitt ISK");
                Account kf = createAccount(2, "KF", "Min KF");

                when(accountRepository.findByUserIdOrderByAccountTypeAscAccountNameAsc(7))
                        .thenReturn(List.of(isk, kf));

                when(accountAllocationOverviewService.getAllocation(1, 7))
                        .thenReturn(createAllocation(
                                true,
                                 category(AssetCategory.STOCK, "75.00", "60.00", "15.00")));

                when(accountAllocationOverviewService.getAllocation(2, 7))
                        .thenReturn(createAllocation(
                                true, 
                                category(AssetCategory.FUND, "20.00", "40.00", "-20.00")));

                List<LiveAlertDto> result = alertService.getLiveAlertsByUserId(7);

                assertEquals(2, result.size());
                
                assertEquals(1, result.get(0).accountId());
                assertEquals("Mitt ISK", result.get(0).accountName());

                assertEquals(2, result.get(1).accountId());
                assertEquals("Min KF", result.get(1).accountName());

                verify(accountAllocationOverviewService).getAllocation(1, 7);
                verify(accountAllocationOverviewService).getAllocation(2, 7);
        }

        private Account createAccount(
                        Integer id,
                        String accountType,
                        String accountName) {

                Account account = new Account(
                                7,
                                accountType,
                                accountName,
                                "SEK");

                ReflectionTestUtils.setField(account, "id", id);

                return account;
        }

        private AccountAllocationCategoryResponse category(
                        AssetCategory category,
                        String actual,
                        String target,
                        String difference) {

                return new AccountAllocationCategoryResponse(
                                category,
                                new BigDecimal("50000.00"),
                                new BigDecimal(actual),
                                new BigDecimal(target),
                                new BigDecimal(difference),
                                BigDecimal.ZERO);
        }

        private AccountAllocationOverviewResponse createAllocation(
                        boolean targetsConfigured,
                        AccountAllocationCategoryResponse... categories) {

                return new AccountAllocationOverviewResponse(
                                1,
                                new BigDecimal("100000.00"),
                                targetsConfigured,
                                List.of(categories));
        }
}
