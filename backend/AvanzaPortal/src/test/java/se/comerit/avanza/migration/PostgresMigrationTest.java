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

    private static final List<String> TABLES = List.of("users", "accounts", "holdings", "alerts", "target_allocations" );

    @BeforeAll
    static void verifyConnections() throws SQLException {
        assertNotEquals(OLD_URL, NEW_URL, "Old and new databse URL must be different. Cannot compare databse to itself.");

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
    void allTableDataShouldBeIdenticalAfterMigration() throws SQLException {
        try (Connection oldConnection = oldConnection();
        Connection newConnection = newConnection()) {
            for (String table  : TABLES) {
                String sql = "SELECT * FROM " + table + " ORDER BY id";

                try (Statement oldStatement = oldConnection.createStatement();
                     Statement newStatement = newConnection.createStatement();

                     ResultSet oldResult = oldStatement.executeQuery(sql);
                     ResultSet newResult = newStatement.executeQuery(sql)) {

                    ResultSetMetaData oldMetaData = oldResult.getMetaData();
                    ResultSetMetaData newMetaData = newResult.getMetaData();

                    int oldColumnCount = oldMetaData.getColumnCount();
                    int newColumnCount = newMetaData.getColumnCount();

                    assertEquals(oldColumnCount, newColumnCount, "Different number of columns in table" + table);

                    int rowNumber = 0;
                    while (true){
                        boolean oldHasRow = oldResult.next();
                        boolean newHasRow = newResult.next();

                        assertEquals(oldHasRow, newHasRow, "Different number of rows in table " + table);

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
                                    "Different in table " + table
                                            + " in row " + rowNumber
                                            + " column " + columnName);
                        }
                    }

                    System.out.println(table + " OK");
                }
            }
        }
    }
}
