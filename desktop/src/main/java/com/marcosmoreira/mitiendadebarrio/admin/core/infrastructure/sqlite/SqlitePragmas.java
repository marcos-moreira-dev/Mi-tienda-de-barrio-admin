package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Configuración mínima recomendada para SQLite local. */
public final class SqlitePragmas {

    private SqlitePragmas() {
    }

    public static void apply(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
            statement.execute("PRAGMA journal_mode = WAL");
            statement.execute("PRAGMA synchronous = NORMAL");
        }
    }
}
