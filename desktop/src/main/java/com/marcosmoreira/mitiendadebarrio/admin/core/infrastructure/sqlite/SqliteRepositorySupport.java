package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/** Utilidades comunes para adaptadores SQLite. */
public abstract class SqliteRepositorySupport {
    protected final SqliteConnectionFactory connectionFactory;
    protected final JdbcExecutor jdbc;
    protected final SqliteTransactionManager transactions;

    protected SqliteRepositorySupport(SqliteConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.jdbc = new JdbcExecutor(connectionFactory);
        this.transactions = new SqliteTransactionManager(connectionFactory);
    }

    protected String normalize(String value) {
        return value == null ? "" : value.strip();
    }

    protected String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    protected String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    protected BigDecimal bigDecimalOrZero(ResultSet rs, String column) throws SQLException {
        BigDecimal value = rs.getBigDecimal(column);
        return value == null ? BigDecimal.ZERO : value;
    }

    protected BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    protected Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    protected void setNullableLong(PreparedStatement statement, int index, Long value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setLong(index, value);
        }
    }
}
