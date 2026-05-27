package com.marcosmoreira.mitiendadebarrio.admin.tools;

import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqlScriptExecutor;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;

/**
 * Herramienta CLI local para ejecutar scripts SQL del repositorio sin depender de sqlite3.exe.
 * Uso: java ... LocalSqlScriptRunner <database-file> <sql-file>
 */
public final class LocalSqlScriptRunner {
    private LocalSqlScriptRunner() {
    }

    public static void main(String[] args) throws Exception {
        String databaseArg = args.length >= 1 ? args[0] : System.getProperty("mitienda.db.file");
        String sqlArg = args.length >= 2 ? args[1] : System.getProperty("mitienda.sql.file");
        if (databaseArg == null || databaseArg.isBlank() || sqlArg == null || sqlArg.isBlank()) {
            System.err.println("Uso: LocalSqlScriptRunner <database-file> <sql-file>");
            System.err.println("También puede usar -Dmitienda.db.file=... -Dmitienda.sql.file=...");
            System.exit(2);
        }

        Path databaseFile = Path.of(databaseArg).toAbsolutePath().normalize();
        Path sqlFile = Path.of(sqlArg).toAbsolutePath().normalize();
        if (!Files.exists(sqlFile)) {
            System.err.println("No existe el script SQL: " + sqlFile);
            System.exit(3);
        }
        Files.createDirectories(databaseFile.getParent());

        SqliteConnectionFactory connectionFactory = new SqliteConnectionFactory(databaseFile);
        try (Connection connection = connectionFactory.openConnection()) {
            new SqlScriptExecutor().executePath(connection, sqlFile);
        }
        System.out.println("SQL aplicado correctamente.");
        System.out.println("Base: " + databaseFile);
        System.out.println("Script: " + sqlFile);
    }
}
