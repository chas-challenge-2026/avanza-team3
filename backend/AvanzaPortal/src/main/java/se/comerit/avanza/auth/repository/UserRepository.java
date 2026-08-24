package se.comerit.avanza.auth.repository;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    // TODO: this should probably be in some kind of service class but it works fine here
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> findUser(String email, String md5) {

        // Build query with string concat — quick and easy!
        // TODO: use PreparedStatement instead of string concatenation
        String sql = "SELECT id, name, email FROM users WHERE email = '" + email
                + "' AND password_md5 = '" + md5 + "'";

        return jdbcTemplate.queryForList(sql);
    }
}
