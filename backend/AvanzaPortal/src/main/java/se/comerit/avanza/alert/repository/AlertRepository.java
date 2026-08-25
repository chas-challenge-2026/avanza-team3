package se.comerit.avanza.alert.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AlertRepository {

    private final JdbcTemplate jdbcTemplate;

    public AlertRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public List<Map<String, Object>> getAlerts(Integer userId) {

    String alertSql = "SELECT id, alert_type, message, dismissed, created_at " +
            "FROM alerts WHERE user_id = ?" + " ORDER BY created_at DESC";
    return jdbcTemplate.queryForList(alertSql, userId);
    }


    //denna ska egentligen ligga i annan repo-klass(accountrepo), men för enkelhetens skull bor den kvar här en stund
    public List<Map<String, Object>> getAccountsByUserId(Integer userId) {

        String accountSql = "SELECT id, account_type FROM accounts WHERE user_id = ?";

        return jdbcTemplate.queryForList(accountSql, userId);
    }

    //denna ska egentligen ligga i annan repo-klass(holdingrepo), men för enkelhetens skull bor den kvar här en stund
    //tror denna finns i HoldingRepo redan, men annan branch så tittar på det när det mergats
    public List<Map<String, Object>> getHoldingsForAlertByUserId(Integer userId) {

        String holdingSql = "SELECT h.account_id, h.quantity, h.avg_buy_price, h.currency, h.ticker " +
                "FROM holdings h WHERE h.account_id IN " +
                "(SELECT id FROM accounts WHERE user_id = ?)";

        return jdbcTemplate.queryForList(holdingSql, userId);
    }

//    String targetSql = "SELECT account_type, target_pct FROM target_allocations WHERE user_id = " + userId;
//    List<Map<String, Object>> targets = jdbcTemplate.queryForList(targetSql);
}
