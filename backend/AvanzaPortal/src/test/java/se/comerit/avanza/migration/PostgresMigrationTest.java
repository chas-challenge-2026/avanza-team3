package se.comerit.avanza.migration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Tag("migration")
@EnabledIfSystemProperty(named = "migration.testing", matches = "true")
public class PostgresMigrationTest {

    private static final String OLD_URL = "jdbc:postgresql://localhost:5432/avanza";
    private static final String NEW_URL = "jdbc:postgresql://localhost:5433/avanza";

    private static final String OLD_USERNAME = "avanza";
    private static final String OLD_PASSWORD = "avanza123";

    private static final String NEW_USERNAME = "avanza";
    private static final String NEW_PASSWORD = "avanza123";

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

}
