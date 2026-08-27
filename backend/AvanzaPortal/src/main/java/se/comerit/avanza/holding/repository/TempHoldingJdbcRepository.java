package se.comerit.avanza.holding.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class TempHoldingJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public TempHoldingJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Tillfällig lösning tills gruppens AccountRepository finns.
    // Används bara för account-dropdownen på holdings-sidan.
    public List<Map<String, Object>> findAccountsByUserId(Integer userId) {
        String sql = "SELECT id, account_type, account_name FROM accounts WHERE user_id = ?";
        return jdbcTemplate.queryForList(sql, userId);
    }
}
