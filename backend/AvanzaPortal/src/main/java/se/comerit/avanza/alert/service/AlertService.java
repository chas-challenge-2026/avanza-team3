package se.comerit.avanza.alert.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import se.comerit.avanza.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlertService {
    private static BigDecimal DRIFT_THRESHOLD = new BigDecimal("5.00");

    private final AlertRepository alertRepository;
    private final AccountRepository accountRepository;
    private final AccountAllocationOverviewService accountAllocationOverviewService;

    public AlertService(
            AlertRepository alertRepository,
            AccountRepository accountRepository,
            AccountAllocationOverviewService accountAllocationOverviewService) {
        this.alertRepository = alertRepository;
        this.accountRepository = accountRepository;
        this.accountAllocationOverviewService = accountAllocationOverviewService;
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    public List<AlertResponse> getAlertsByUserId(Integer userId) {
        return alertRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toAlertResponse)
                .toList();
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    public Page<AlertResponse> getAlertsByUserId(
            Integer userId,
            boolean dismissed,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        return alertRepository
                .findByUserIdAndDismissedOrderByCreatedAtDesc(
                        userId,
                        dismissed,
                        pageable)
                .map(this::toAlertResponse);
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    public void dismissAlert(Integer alertId, Integer userId) {
        Alert alert = alertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        alert.setDismissed(true);
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional(readOnly = true)
    public List<LiveAlertDto> getLiveAlertsByUserId(Integer userId) {
        List<Account> accounts = accountRepository.findByUserIdOrderByAccountTypeAscAccountNameAsc(userId);

        List<LiveAlertDto> liveAlerts = new ArrayList<>();

        for (Account account : accounts) {
            AccountAllocationOverviewResponse allocation = accountAllocationOverviewService
                    .getAllocation(account.getId(), userId);

            if (!allocation.targetsConfigured()) {
                continue;
            }

            for (AccountAllocationCategoryResponse category : allocation.categories()) {
                if (category.category() != AssetCategory.STOCK && category.category() != AssetCategory.FUND) {
                    continue;
                }

                BigDecimal difference = category.differencePctPoints();
                if (difference == null || difference.abs().compareTo(DRIFT_THRESHOLD) <= 0) {
                    continue;
                }

                String accountName = account.getAccountName() != null
                        ? account.getAccountName()
                        : account.getAccountType();

                String direction = difference.signum() > 0
                        ? "över"
                        : "under";

                String message = String.format(
                        "%s ligger %.2f procentenheter %s målet för %s" + "(nuvarande %.2f, mål %.2f%%).",
                        category.category(),
                        difference.abs(),
                        direction,
                        accountName,
                        category.actualPct(),
                        category.targetPct());

                LiveAlertDto liveAlert = new LiveAlertDto(
                        "LIVE_DRIFT",
                        account.getId(),
                        accountName,
                        category.category().name(),
                        category.actualPct(),
                        category.targetPct(),
                        difference,
                        message,
                        false,
                        "Nu");

                liveAlerts.add(liveAlert);
            }
        }

        return liveAlerts;
    }

    private AlertResponse toAlertResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getAlertType(),
                alert.getMessage(),
                alert.isDismissed(),
                alert.getCreatedAt());
    }
}
