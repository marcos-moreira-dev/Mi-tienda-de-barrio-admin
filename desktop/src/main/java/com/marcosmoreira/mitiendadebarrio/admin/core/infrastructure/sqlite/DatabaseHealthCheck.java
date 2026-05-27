package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Verificación mínima de SQLite y del esquema local instalado. */
public final class DatabaseHealthCheck {

    private final SqliteConnectionFactory factory;

    public DatabaseHealthCheck(SqliteConnectionFactory factory) {
        this.factory = factory;
    }

    public String describeStatus() {
        try (Connection connection = factory.openConnection(); Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
            String version = readSchemaVersion(connection);
            List<String> integrityErrors = readIntegrityErrors(connection);
            if (!integrityErrors.isEmpty()) {
                return "Base de datos local disponible, pero con alertas de integridad: " + String.join(" | ", integrityErrors);
            }
            return "Base de datos local disponible. Versión de esquema: " + version + ".";
        } catch (Exception ex) {
            return "Base de datos local no disponible: " + ex.getMessage();
        }
    }

    private String readSchemaVersion(Connection connection) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT version FROM schema_version WHERE id = 1 LIMIT 1")) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
            return "SIN_REGISTRO";
        } catch (Exception ex) {
            return "SIN_VERSION";
        }
    }

    private List<String> readIntegrityErrors(Connection connection) {
        List<String> errors = new ArrayList<>();
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery("PRAGMA integrity_check")) {
            while (resultSet.next()) {
                String value = resultSet.getString(1);
                if (!"ok".equalsIgnoreCase(value)) {
                    errors.add("integrity_check: " + value);
                }
            }
        } catch (Exception ex) {
            errors.add("integrity_check no disponible: " + ex.getMessage());
        }

        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery("PRAGMA foreign_key_check")) {
            while (resultSet.next()) {
                errors.add("foreign_key_check: tabla=" + resultSet.getString(1) + ", fila=" + resultSet.getString(2));
            }
        } catch (Exception ex) {
            errors.add("foreign_key_check no disponible: " + ex.getMessage());
        }
        return errors;
    }
}
