package se.comerit.avanza.alert.service;

import org.springframework.stereotype.Service;
import se.comerit.avanza.account.service.AccountService;
import se.comerit.avanza.alert.model.Alert;
import se.comerit.avanza.alert.repository.AlertRepository;
import se.comerit.avanza.alert.repository.TempJdbcRepo;
import se.comerit.avanza.holding.service.HoldingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class AlertService {

    private static final double DRIFT_THRESHOLD = 0.07;

    private final AlertRepository alertRepository;
    private final TempJdbcRepo tempJdbcRepo;
    private final HoldingService holdingService;
    AccountService accountService;

    public AlertService(AlertRepository alertRepository, TempJdbcRepo tempJdbcRepo, HoldingService holdingService, AccountService accountService) {
        this.tempJdbcRepo = tempJdbcRepo;
        this.alertRepository = alertRepository;
        this.holdingService = holdingService;
        this.accountService = accountService;
    }

    public List<Map<String, Object>> getAccountsByUserId(Integer userId) {
        return accountService.getAccountMapsByUserId(userId);
    }

    public List<Map<String, Object>> getHoldingsForAlertByUserId(Integer userId) {
        return holdingService.getHoldingsByUserId(userId);
    }

    public List<Alert> getAlertsByUserId(Integer userId) {
        return alertRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Map<String, Object>> getTargetByUserId(Integer userId) {
        return tempJdbcRepo.getTargetByUserId(userId);
    }

    public void dismissAlert(Integer alertId) {
        alertRepository.findById(alertId).ifPresent(alert -> {
            alert.setDismissed(true);
            alertRepository.save(alert);
        });
    }

    public List<Map<String, Object>> getLiveAlertsByUserId(Integer userId) {
        List<Map<String, Object>> accounts =
                accountService.getAccountMapsByUserId(userId);

        List<Map<String, Object>> holdings =
                holdingService.getHoldingsByUserId(userId);

        List<Map<String, Object>> targets =
                tempJdbcRepo.getTargetByUserId(userId);

        Map<String, Double> prices = createPriceMap();

        double usdToSek = getUsdToSekRate();

        Map<Integer, String> accountTypeById = createAccountTypeById(accounts);

        Map<String, Double> typeTotals = new HashMap<>();
        double grandTotal = 0.0;

        for (Map<String, Object> h : holdings) {
            String ticker = (String) h.get("ticker");
            String currency = (String) h.get("currency");
            double qty = ((java.math.BigDecimal) h.get("quantity")).doubleValue();
            double price = prices.getOrDefault(ticker, 100.0);
            double valueSek = "USD".equals(currency) ? qty * price * usdToSek : qty * price;

            Integer accId = (Integer) h.get("account_id");
            String accType = accountTypeById.getOrDefault(accId, "Depa");
            typeTotals.put(accType, typeTotals.getOrDefault(accType, 0.0) + valueSek);
            grandTotal += valueSek;
        }

        Map<String, Double> targetMap = new HashMap<>();
        for (Map<String, Object> t : targets) {
            targetMap.put((String) t.get("account_type"),
                    ((java.math.BigDecimal) t.get("target_pct")).doubleValue());
        }

        List<Map<String, Object>> liveAlerts = new ArrayList<>();
        for (String accType : new String[]{"ISK", "KF", "Depa"}) {
            double actual = grandTotal > 0
                    ? (typeTotals.getOrDefault(accType, 0.0) / grandTotal) * 100.0
                    : 0.0;
            double target = targetMap.getOrDefault(accType, 0.0);
            double drift = Math.abs(actual - target) / 100.0;

            if (drift > DRIFT_THRESHOLD) {
                Map<String, Object> liveAlert = new HashMap<>();
                liveAlert.put("alert_type", "LIVE_DRIFT");
                liveAlert.put("message", String.format(
                        "%s-allokering: faktisk %.1f%% vs mål %.1f%% (avvikelse %.1f%%) — ombalansering rekommenderas",
                        accType, actual, target, drift * 100));
                liveAlert.put("dismissed", false);
                liveAlert.put("created_at", "Nu");
                liveAlerts.add(liveAlert);
            }
        }
        return liveAlerts;
    }

    public int getDriftThresholdPercent(){
        return (int) (DRIFT_THRESHOLD * 100);
    }

    private Map<String, Double> createPriceMap() {

        Map<String, Double> prices = new HashMap<>();
        prices.put("ERIC-B", 74.20);
        prices.put("VOLV-B", 268.50);
        prices.put("AAPL", 187.32);
        prices.put("SWED-A", 193.10);
        prices.put("SAND", 212.80);
        return prices;
    }

    private double getUsdToSekRate() {
        return 10.45;
    }

    private Map<Integer, String> createAccountTypeById(List<Map<String, Object>> accounts) {
        Map<Integer, String> accountTypeById = new HashMap<>();

        for (Map<String, Object> acc : accounts) {
            accountTypeById.put((Integer) acc.get("id"),
                    (String) acc.get("account_type"));
        }
        return accountTypeById;
    }
}
