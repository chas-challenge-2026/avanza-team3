package se.comerit.avanza.alert.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class TempJdbcRepo {
    private final JdbcTemplate jdbcTemplate;

    public TempJdbcRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //denna ska egentligen ligga i annan repo-klass(targetrepo eller targetAllocationRepo, måste bestämmas med andra i gruppen), men för enkelhetens skull bor den kvar här en stund
    public List<Map<String, Object>> getTargetByUserId(Integer userId) {

        String targetSql = "SELECT account_type, target_pct FROM target_allocations WHERE user_id = ?";

        return jdbcTemplate.queryForList(targetSql, userId);
    }
}
