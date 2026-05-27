package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite;

import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Fábrica de conexiones SQLite. */
public final class SqliteConnectionFactory {

    private final Path databaseFile;

    public SqliteConnectionFactory(Path databaseFile) {
        this.databaseFile = databaseFile;
    }

    public Connection openConnection() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.toAbsolutePath());
            SqlitePragmas.apply(connection);
            return connection;
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo abrir la base de datos local.", ex);
        }
    }

    public Path databaseFile() {
        return databaseFile;
    }

    public void closeQuietly() {
        // En esta primera versión las conexiones se abren por operación.
    }
}
