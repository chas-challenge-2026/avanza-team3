package se.comerit.avanza.migration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@Tag("migration")
@EnabledIfSystemProperty(named = "migration.testing", matches = "true")
public class PostgresMigrationTest {


}
