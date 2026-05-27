package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite;

import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.sql.Connection;

/** Carga datos mínimos idempotentes para una instalación real de cliente. */
public final class LocalDatabaseSeeder {
    private static final String INITIAL_CLIENT_SEED = "/db/seeds/V001__seed_inicial_cliente.sql";

    private final SqliteConnectionFactory connectionFactory;
    private final SqlScriptExecutor executor;

    public LocalDatabaseSeeder(SqliteConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.executor = new SqlScriptExecutor();
    }

    public void seedInitialClientData() {
        try (Connection connection = connectionFactory.openConnection()) {
            executor.executeResource(connection, INITIAL_CLIENT_SEED);
        } catch (Exception ex) {
            if (ex instanceof InfrastructureException infrastructureException) {
                throw infrastructureException;
            }
            throw new InfrastructureException("No se pudieron preparar los datos iniciales del cliente.", ex);
        }
    }
}
