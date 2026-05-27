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
 * Pequeño ejecutor JDBC local para evitar repetición de try-with-resources en los repositorios SQLite.
 * No es un ORM: mantiene SQL explícito y solo centraliza el patrón común de conexión, binding y mapeo.
 */
public final class JdbcExecutor {
    private final SqliteConnectionFactory connectionFactory;

    public JdbcExecutor(SqliteConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public <T> List<T> query(String sql, StatementBinder binder, RowMapper<T> mapper, String errorMessage) {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (binder != null) {
                binder.bind(statement);
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<T> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(mapper.map(rs));
                }
                return items;
            }
        } catch (SQLException ex) {
            throw new InfrastructureException(errorMessage, ex);
        }
    }

    public <T> Optional<T> queryOne(String sql, StatementBinder binder, RowMapper<T> mapper, String errorMessage) {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (binder != null) {
                binder.bind(statement);
            }
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapper.map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new InfrastructureException(errorMessage, ex);
        }
    }

    public int update(String sql, StatementBinder binder, String errorMessage) {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (binder != null) {
                binder.bind(statement);
            }
            return statement.executeUpdate();
        } catch (SQLException ex) {
            throw new InfrastructureException(errorMessage, ex);
        }
    }

    public long insertReturningId(String sql, StatementBinder binder, String errorMessage) {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (binder != null) {
                binder.bind(statement);
            }
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new InfrastructureException("No se pudo obtener el ID generado por SQLite.");
            }
        } catch (SQLException ex) {
            throw new InfrastructureException(errorMessage, ex);
        }
    }
}
