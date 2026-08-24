package se.comerit.avanza.holding.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class HoldingRepository {

    private final JdbcTemplate jdbcTemplate;

    public HoldingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String,Object>> findHoldingsByUserId(Integer userId){
        String sql = "SELECT h.id, h.ticker, h.instrument_name, h.quantity, h.avg_buy_price, " +
                "h.currency, a.account_type, a.account_name " +
                "FROM holdings h " +
                "JOIN accounts a ON h.account_id = a.id " +
                "WHERE a.user_id = " + userId + " " +
                "ORDER BY a.account_type, h.ticker";

        return jdbcTemplate.queryForList(sql, userId);
    }


    //rad 44 i listHoldings(HttpSession session, Model model)
//    String accountSql = "SELECT id, account_type, account_name FROM accounts WHERE user_id = " + userId;
//    List<Map<String, Object>> accounts = jdbcTemplate.queryForList(accountSql);


    //rad 94 i addHolding(Integer accountId, String ticker, String instrumentName, String quantity, String avgBuyPrice, String currency, HttpSession session, Model model)
//    String sql = "INSERT INTO holdings (account_id, ticker, instrument_name, quantity, avg_buy_price, currency) " +
//            "VALUES (" + accountId + ", '" + ticker.toUpperCase() + "', '" + instrumentName + "', " +
//            quantity + ", " + avgBuyPrice + ", '" + currency + "')";


    //rad 115 i deleteHolding(Integer holdingId, HttpSession session)
//    String sql = "DELETE FROM holdings WHERE id = " + holdingId;
//        jdbcTemplate.execute(sql);
}
