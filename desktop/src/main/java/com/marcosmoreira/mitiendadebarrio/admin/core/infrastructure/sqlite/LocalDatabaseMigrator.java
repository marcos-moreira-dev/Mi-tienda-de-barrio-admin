package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite;

import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Inicializa la base SQLite local usando la V001 consolidada empaquetada en resources.
 *
 * <p>Como MiTienda todavía no tiene instalaciones reales de clientes, el producto se
 * está consolidando sobre una V001 canónica. Este migrador no intenta actuar como
 * Flyway: solo instala la base limpia cuando no existe, registra la versión local y
 * protege contra reejecuciones peligrosas sobre bases antiguas o desconocidas.</p>
 */
public final class LocalDatabaseMigrator {
    public static final String EXPECTED_VERSION = "V001";
    public static final String EXPECTED_MIGRATION_NAME = "V001__schema_erp_local_sqlite_consolidado.sql";

    private static final String V001_SCHEMA = "/db/migrations/" + EXPECTED_MIGRATION_NAME;

    private final SqliteConnectionFactory connectionFactory;
    private final SqlScriptExecutor executor;

    public LocalDatabaseMigrator(SqliteConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.executor = new SqlScriptExecutor();
    }

    public void migrate() {
        try (Connection connection = connectionFactory.openConnection()) {
            if (hasSchemaVersionTable(connection)) {
                ensureCompatibleVersion(connection);
                ensureSchemaIntegrity(connection);
                return;
            }

            if (hasUserTables(connection)) {
                throw new InfrastructureException(
                        "La base local existe, pero no tiene schema_version. "
                                + "Probablemente fue creada con una versión anterior al esquema ERP local consolidado. "
                                + "Si esta es una base de desarrollo, ejecute scripts/reset-runtime-data.bat y vuelva a abrir la aplicacion para crear la V001 consolidada limpia.");
            }

            executor.executeResource(connection, V001_SCHEMA);
            registerExpectedVersion(connection, "Base ERP local SQLite creada desde V001 consolidada.");
            ensureSchemaIntegrity(connection);
        } catch (Exception ex) {
            if (ex instanceof InfrastructureException infrastructureException) {
                throw infrastructureException;
            }
            throw new InfrastructureException("No se pudo inicializar la base de datos local.", ex);
        }
    }

    public String installedVersion() {
        try (Connection connection = connectionFactory.openConnection()) {
            if (!hasSchemaVersionTable(connection)) {
                return "SIN_VERSION";
            }
            return readInstalledVersion(connection).orElse("SIN_REGISTRO");
        } catch (Exception ex) {
            throw new InfrastructureException("No se pudo leer la versión de la base local.", ex);
        }
    }

    private void ensureCompatibleVersion(Connection connection) throws SQLException {
        Optional<String> installedVersion = readInstalledVersion(connection);
        if (installedVersion.isEmpty()) {
            registerExpectedVersion(connection, "Registro de versión recuperado por el migrador local.");
            return;
        }

        if (!EXPECTED_VERSION.equals(installedVersion.get())) {
            throw new InfrastructureException(
                    "La base local tiene versión " + installedVersion.get()
                            + ", pero esta aplicación espera " + EXPECTED_VERSION + ". "
                            + "No se aplicarán cambios automáticos para evitar dañar datos locales.");
        }
    }

    private boolean hasSchemaVersionTable(Connection connection) throws SQLException {
        return tableExists(connection, "schema_version");
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        String sql = "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean hasUserTables(Connection connection) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' LIMIT 1";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next();
        }
    }

    private Optional<String> readInstalledVersion(Connection connection) throws SQLException {
        String sql = "SELECT version FROM schema_version WHERE id = 1 LIMIT 1";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return Optional.ofNullable(resultSet.getString("version"));
            }
            return Optional.empty();
        }
    }

    private void registerExpectedVersion(Connection connection, String observacion) throws SQLException {
        String sql = """
                INSERT INTO schema_version (id, version, nombre_migracion, estado, observacion)
                VALUES (1, ?, ?, 'APLICADA', ?)
                ON CONFLICT(id) DO UPDATE SET
                    version = excluded.version,
                    nombre_migracion = excluded.nombre_migracion,
                    estado = excluded.estado,
                    observacion = excluded.observacion,
                    updated_at = datetime('now')
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, EXPECTED_VERSION);
            statement.setString(2, EXPECTED_MIGRATION_NAME);
            statement.setString(3, observacion);
            statement.executeUpdate();
        }
    }

    private void ensureSchemaIntegrity(Connection connection) throws SQLException {
        List<String> errors = new ArrayList<>();

        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery("PRAGMA integrity_check")) {
            while (resultSet.next()) {
                String value = resultSet.getString(1);
                if (!"ok".equalsIgnoreCase(value)) {
                    errors.add("integrity_check: " + value);
                }
            }
        }

        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery("PRAGMA foreign_key_check")) {
            while (resultSet.next()) {
                String table = resultSet.getString(1);
                String rowId = resultSet.getString(2);
                String parent = resultSet.getString(3);
                errors.add("foreign_key_check: tabla=" + table + ", fila=" + rowId + ", referencia=" + parent);
            }
        }

        if (!errors.isEmpty()) {
            throw new InfrastructureException("La base local no superó la verificación de integridad: " + String.join(" | ", errors));
        }
    }
}
