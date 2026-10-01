package se.comerit.avanza.migration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Tag("migration")
@EnabledIfSystemProperty(named = "migration.testing", matches = "true")
public class PostgresMigrationTest {

    private static final String OLD_URL = "jdbc:postgresql://127.0.0.1:15432/avanza";
    private static final String NEW_URL = "jdbc:postgresql://127.0.0.1:15433/avanza";

    private static final String OLD_USERNAME = "avanza";
    private static final String OLD_PASSWORD = "avanza123";

    private static final String NEW_USERNAME = "avanza";
    private static final String NEW_PASSWORD = "avanza123";

    private static final List<String> UNCHANGED_TABLES = List.of("users", "accounts", "alerts", "target_allocations" );

    @BeforeAll
    static void verifyConnections() throws SQLException {
        assertNotEquals(OLD_URL, NEW_URL, "Old and new database URL must be different. Cannot compare database to itself.");

        try (Connection oldConnection = oldConnection();
             Connection newConnection = newConnection())
        {
            System.out.println("Old= " + databaseVersion(oldConnection));
            System.out.println("New= " + databaseVersion(newConnection));
        }

    }

    private static Connection oldConnection() throws SQLException {
        return DriverManager.getConnection(OLD_URL, OLD_USERNAME, OLD_PASSWORD);
    }

    private static Connection newConnection() throws SQLException {
        return DriverManager.getConnection(NEW_URL, NEW_USERNAME, NEW_PASSWORD);
    }

    private static String databaseVersion(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT version()")) {
            resultSet.next();
            return resultSet.getString(1);
        }
    }

    @Test
    void unchangedTableDataShouldBeIdenticalAfterMigration() throws SQLException {
        try (Connection oldConnection = oldConnection();
             Connection newConnection = newConnection()) {

            for (String table : UNCHANGED_TABLES) {
                assertQueryResultsEqual(
                        oldConnection,
                        newConnection,
                        "SELECT * FROM " + table + " ORDER BY id",
                        table
                );
            }
        }
    }

    @Test
    void legacyHoldingDataShouldBePreservedAfterInstrumentMigration() throws SQLException {
        String sql = """
                SELECT id, account_id, ticker, instrument_name, quantity, avg_buy_price, currency
                FROM holdings
                ORDER BY id
                """;

        try (Connection oldConnection = oldConnection();
         Connection newConnection = newConnection()) {
            assertQueryResultsEqual(oldConnection, newConnection, sql, "holdings legacy columns");
        }
    }

    @Test
    void allHoldingsShouldBeLinkedToMatchingUnknownInstrument() throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM holdings h
                LEFT JOIN instruments i ON i.id = h.instrument_id
                WHERE h.instrument_id IS NULL
                   OR i.id IS NULL
                   OR UPPER(h.ticker) <> UPPER(i.ticker)
                   OR h.instrument_name IS DISTINCT FROM i.name
                   OR h.currency IS DISTINCT FROM i.currency
                   OR i.instrument_type <> 'UNKNOWN'
                   OR i.sector <> 'UNKNOWN'
                """;

        try (Connection connection = newConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            resultSet.next();
            assertEquals(
                    0,
                    resultSet.getInt(1),
                    "Every migrated holding should point to its matching UNKNOWN instrument"
            );
        }
    }

    @Test
    void instrumentCountShouldMatchDistinctHoldingTickers() throws SQLException {
        String oldSql = "SELECT COUNT(DISTINCT UPPER(ticker)) FROM holdings";
        String newSql = "SELECT COUNT(*) FROM instruments";

        try (Connection oldConnection = oldConnection();
             Connection newConnection = newConnection();
             Statement oldStatement = oldConnection.createStatement();
             Statement newStatement = newConnection.createStatement();
             ResultSet oldResult = oldStatement.executeQuery(oldSql);
             ResultSet newResult = newStatement.executeQuery(newSql)) {

            oldResult.next();
            newResult.next();

            assertEquals(
                    oldResult.getInt(1),
                    newResult.getInt(1),
                    "There should be exactly one instrument per distinct ticker after migration"
            );
        }
    }

    private static void assertQueryResultsEqual(
            Connection oldConnection,
            Connection newConnection,
            String sql,
            String description
    ) throws SQLException {

                try (Statement oldStatement = oldConnection.createStatement();
                     Statement newStatement = newConnection.createStatement();

                     ResultSet oldResult = oldStatement.executeQuery(sql);
                     ResultSet newResult = newStatement.executeQuery(sql)) {

                    ResultSetMetaData oldMetaData = oldResult.getMetaData();
                    ResultSetMetaData newMetaData = newResult.getMetaData();

                    int oldColumnCount = oldMetaData.getColumnCount();
                    int newColumnCount = newMetaData.getColumnCount();

                    assertEquals(oldColumnCount, newColumnCount, "Different number of selected columns in " + description);

                    int rowNumber = 0;
                    while (true){
                        boolean oldHasRow = oldResult.next();
                        boolean newHasRow = newResult.next();

                        assertEquals(oldHasRow, newHasRow, "Different number of rows in " + description);

                        if (!oldHasRow) {
                            break;
                        }
                        rowNumber++;

                        for (int i = 1; i <= oldColumnCount; i++) {
                            Object oldValue = oldResult.getObject(i);
                            Object newValue = newResult.getObject(i);

                            String columnName = oldMetaData.getColumnName(i);

                            assertEquals(
                                    oldValue,
                                    newValue,
                                    "Difference in " + description
                                            + " row " + rowNumber
                                            + " column " + columnName);
                        }
                    }
                    System.out.println(description + " OK");
                }
            }
}
