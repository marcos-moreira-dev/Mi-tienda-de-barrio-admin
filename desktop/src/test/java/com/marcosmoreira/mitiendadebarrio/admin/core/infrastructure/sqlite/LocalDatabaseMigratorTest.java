package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalDatabaseMigratorTest {

    @TempDir
    Path tempDir;

    @Test
    void creaV001ConsolidadaEnBaseNueva() throws Exception {
        Path db = tempDir.resolve("data").resolve("mi_tienda_de_barrio_admin.sqlite");
        Files.createDirectories(db.getParent());
        SqliteConnectionFactory factory = new SqliteConnectionFactory(db);
        LocalDatabaseMigrator migrator = new LocalDatabaseMigrator(factory);

        migrator.migrate();

        assertTrue(Files.exists(db));
        assertEquals(LocalDatabaseMigrator.EXPECTED_VERSION, migrator.installedVersion());

        try (Connection connection = factory.openConnection(); Statement statement = connection.createStatement()) {
            assertTableExists(statement, "schema_version");
            assertTableExists(statement, "usuario_local");
            assertTableExists(statement, "auditoria_evento");
            assertTableExists(statement, "venta_interna");
            assertTableExists(statement, "asiento_contable");
            assertIntegrityOk(statement);
            assertForeignKeyCheckOk(statement);
        }
    }

    private static void assertTableExists(Statement statement, String tableName) throws Exception {
        try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='" + tableName + "'")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1), "Debe existir tabla " + tableName);
        }
    }

    private static void assertIntegrityOk(Statement statement) throws Exception {
        try (ResultSet rs = statement.executeQuery("PRAGMA integrity_check")) {
            assertTrue(rs.next());
            assertEquals("ok", rs.getString(1));
        }
    }

    private static void assertForeignKeyCheckOk(Statement statement) throws Exception {
        try (ResultSet rs = statement.executeQuery("PRAGMA foreign_key_check")) {
            if (rs.next()) {
                String table = rs.getString(1);
                String rowId = rs.getString(2);
                throw new AssertionError("foreign_key_check encontro violacion en tabla=" + table + ", fila=" + rowId);
            }
        }
    }
}

