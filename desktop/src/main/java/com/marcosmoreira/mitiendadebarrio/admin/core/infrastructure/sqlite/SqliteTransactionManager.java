package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite;

import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.sql.Connection;
import java.sql.SQLException;

/** Manejo explícito de transacciones locales SQLite para flujos que necesitan atomicidad. */
public final class SqliteTransactionManager {
    private final SqliteConnectionFactory connectionFactory;

    public SqliteTransactionManager(SqliteConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public <T> T inTransaction(SqliteTransactionalWork<T> work, String errorMessage) {
        try (Connection connection = connectionFactory.openConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                T result = work.execute(connection);
                connection.commit();
                connection.setAutoCommit(previousAutoCommit);
                return result;
            } catch (Exception ex) {
                connection.rollback();
                if (ex instanceof InfrastructureException infrastructureException) {
                    throw infrastructureException;
                }
                throw new InfrastructureException(errorMessage, ex);
            }
        } catch (SQLException ex) {
            throw new InfrastructureException(errorMessage, ex);
        }
    }

    @FunctionalInterface
    public interface SqliteTransactionalWork<T> {
        T execute(Connection connection) throws Exception;
    }
}
