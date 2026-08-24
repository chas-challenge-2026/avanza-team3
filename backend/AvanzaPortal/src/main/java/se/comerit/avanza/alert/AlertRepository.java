package se.comerit.avanza.alert;

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


    //denna ska egentligen ligga i annan repo-klass, men för enkelhetens skull bor den kvar här en stund
    public List<Map<String, Object>> getAccountsByUserId(Integer userId) {

        String accountSql = "SELECT id, account_type FROM accounts WHERE user_id = ?";

        return jdbcTemplate.queryForList(accountSql, userId);
    }

}
