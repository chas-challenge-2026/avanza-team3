package se.comerit.avanza.alert.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.comerit.avanza.account.service.AccountService;
import se.comerit.avanza.alert.dto.AlertResponse;
import se.comerit.avanza.alert.model.Alert;
import se.comerit.avanza.alert.repository.AlertRepository;
import se.comerit.avanza.holding.service.HoldingService;
import se.comerit.avanza.targetallocation.service.TargetAllocationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class AlertService {

    private static final BigDecimal DRIFT_THRESHOLD = new BigDecimal("0.07");

    private final AlertRepository alertRepository;
    private final HoldingService holdingService;
    private final AccountService accountService;
    private final TargetAllocationService targetAllocationService;

    public AlertService(AlertRepository alertRepository, HoldingService holdingService, AccountService accountService, TargetAllocationService targetAllocationService) {
        this.alertRepository = alertRepository;
        this.holdingService = holdingService;
        this.accountService = accountService;
        this.targetAllocationService = targetAllocationService;
    }

    public List<Map<String, Object>> getAccountsByUserId(Integer userId) {
        return accountService.getAccountMapsByUserId(userId);
    }

    public List<Map<String, Object>> getHoldingsForAlertByUserId(Integer userId) {
        return holdingService.getHoldingsByUserId(userId);
    }

    @Transactional
    @Cacheable(value = "alertsByUser", key = "#userId + '-' + #dismissed + '-' + #page + '-' + #size")
    public Page<AlertResponse> getAlertsByUserId(Integer userId, boolean dismissed, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return alertRepository
                .findByUserIdAndDismissedOrderByCreatedAtDesc(
                        userId,
                        dismissed,
                        pageable)
                .map(this::toAlertResponse);
    }

    public List<Map<String, Object>> getTargetByUserId(Integer userId) {
        return targetAllocationService.getTargetMapsByUserId(userId);
    }

    @Transactional
    @CacheEvict(value = "alertsByUser", allEntries = true)
    public void dismissAlert(Integer alertId, Integer userId) {
        Alert alert = alertRepository.findByIdAndUserId(alertId, userId).orElseThrow(() -> new IllegalArgumentException("Alert not found"));
            alert.setDismissed(true);
    }

    public List<Map<String, Object>> getLiveAlertsByUserId(Integer userId) {
        List<Map<String, Object>> accounts =
                accountService.getAccountMapsByUserId(userId);

        List<Map<String, Object>> holdings =
                holdingService.getHoldingsByUserId(userId);

        List<Map<String, Object>> targets =
                targetAllocationService.getTargetMapsByUserId(userId);

        Map<String, BigDecimal> prices = createPriceMap();

        BigDecimal usdToSek = getUsdToSekRate();

        Map<Integer, String> accountTypeById =
                createAccountTypeById(accounts);

        Map<String, BigDecimal> typeTotals = new HashMap<>();

        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Map<String, Object> holding : holdings) {

            String ticker =
                    (String) holding.get("ticker");
            String currency =
                    (String) holding.get("currency");

            BigDecimal quantity =
                    toBigDecimal(holding.get("quantity"));

            BigDecimal price = prices.getOrDefault(ticker, new BigDecimal("100.00"));

            BigDecimal valueSek;

            if ("USD".equals(currency)) {
                valueSek = quantity
                        .multiply(price)
                        .multiply(usdToSek);
            } else {

                valueSek = quantity.multiply(price);}

            Integer accountId = (Integer) holding.get("account_id");

            String accountType = accountTypeById.getOrDefault(accountId, "Depa");

            BigDecimal currentTotal = typeTotals.getOrDefault(accountType, BigDecimal.ZERO);

            typeTotals.put(accountType, currentTotal.add(valueSek));
            grandTotal = grandTotal.add(valueSek);
        }

        Map<String, BigDecimal> targetMap = new HashMap<>();
        for (Map<String, Object> target : targets) {
            String accountType = (String) target.get("account_type");
            BigDecimal targetPct = toBigDecimal(target.get("target_pct"));
            targetMap.put(accountType, targetPct);
        }
        List<Map<String, Object>> liveAlerts = new ArrayList<>();
        for (String accType : new String[]{"ISK", "KF", "Depa"}) {

            BigDecimal actual;
            if (grandTotal.compareTo(BigDecimal.ZERO) > 0) {
                actual = typeTotals.getOrDefault(accType, BigDecimal.ZERO).divide(grandTotal, 6, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
            } else {
                actual = BigDecimal.ZERO;
            }
            BigDecimal target = targetMap.getOrDefault(accType, BigDecimal.ZERO);
            BigDecimal drift = actual.subtract(target).abs().divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
            if (drift.compareTo(DRIFT_THRESHOLD) > 0) {

                Map<String, Object> liveAlert = new HashMap<>();
                liveAlert.put("alert_type", "LIVE_DRIFT");
                liveAlert.put("message", String.format("%s-allokering: faktisk %.1f%% vs mål %.1f%% (avvikelse %.1f%%) — ombalansering rekommenderas",
                        accType, actual.doubleValue(), target.doubleValue(), drift.multiply(new BigDecimal("100")).doubleValue()));
                liveAlert.put("dismissed", false);
                liveAlert.put("created_at", "Nu");
                liveAlerts.add(liveAlert);
            }
        }
        return liveAlerts;
    }

    public int getDriftThresholdPercent(){
        return DRIFT_THRESHOLD.multiply(new BigDecimal("100")).intValue();
    }

    private AlertResponse toAlertResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getAlertType(),
                alert.getMessage(),
                alert.isDismissed(),
                alert.getCreatedAt()
        );
    }

    private Map<String, BigDecimal> createPriceMap() {

        Map<String, BigDecimal> prices = new HashMap<>();
        prices.put("ERIC-B", new BigDecimal("74.20"));
        prices.put("VOLV-B", new BigDecimal("268.50"));
        prices.put("AAPL", new BigDecimal("187.32"));
        prices.put("SWED-A", new BigDecimal("193.10"));
        prices.put("SAND", new BigDecimal("212.80"));
        return prices;
    }

    private BigDecimal getUsdToSekRate() {
        return new BigDecimal("10.45");
    }

    private Map<Integer, String> createAccountTypeById(List<Map<String, Object>> accounts) {
        Map<Integer, String> accountTypeById = new HashMap<>();

        for (Map<String, Object> acc : accounts) {
            accountTypeById.put((Integer) acc.get("id"),
                    (String) acc.get("account_type"));
        }
        return accountTypeById;
    }

    private BigDecimal toBigDecimal(Object value) {

        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }

        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }

        return new BigDecimal(value.toString());
    }
}