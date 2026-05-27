package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite;

import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Ejecuta scripts SQL locales empaquetados o ubicados en el repositorio. */
public final class SqlScriptExecutor {

    public void executeResource(Connection connection, String resourcePath) {
        try (InputStream inputStream = SqlScriptExecutor.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new InfrastructureException("No se encontró el recurso SQL: " + resourcePath);
            }
            execute(connection, new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new InfrastructureException("No se pudo leer el recurso SQL: " + resourcePath, ex);
        }
    }

    public void executePath(Connection connection, Path sqlFile) {
        try {
            execute(connection, Files.readString(sqlFile, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new InfrastructureException("No se pudo leer el script SQL: " + sqlFile, ex);
        }
    }

    public void execute(Connection connection, String rawSql) {
        String sql = stripLineComments(rawSql);
        try (Statement statement = connection.createStatement()) {
            for (String part : sql.split(";")) {
                String clean = part.strip();
                if (!clean.isBlank()) {
                    statement.execute(clean);
                }
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo ejecutar el script SQL local.", ex);
        }
    }

    private String stripLineComments(String rawSql) {
        StringBuilder builder = new StringBuilder();
        for (String line : rawSql.split("\\R")) {
            String clean = line.stripLeading();
            if (!clean.startsWith("--")) {
                builder.append(line).append(System.lineSeparator());
            }
        }
        return builder.toString();
    }
}
