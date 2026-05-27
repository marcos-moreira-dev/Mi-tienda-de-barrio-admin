package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Encapsula el llenado de parámetros de un PreparedStatement. */
@FunctionalInterface
public interface StatementBinder {
    void bind(PreparedStatement statement) throws SQLException;
}
