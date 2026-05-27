package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;

/** Convierte una fila JDBC en un objeto de dominio o proyección. */
@FunctionalInterface
public interface RowMapper<T> {
    T map(ResultSet rs) throws SQLException;
}
