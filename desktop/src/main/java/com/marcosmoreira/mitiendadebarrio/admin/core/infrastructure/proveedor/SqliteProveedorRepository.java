package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.proveedor;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.proveedor.ProveedorRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.proveedor.EstadoProveedor;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.proveedor.Proveedor;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Adaptador SQLite para proveedores. */
public final class SqliteProveedorRepository extends SqliteRepositorySupport implements ProveedorRepository {

    public SqliteProveedorRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<Proveedor> findAll(boolean includeInactive) {
        String sql = """
                SELECT id, nombre, telefono, whatsapp, direccion, observacion, estado
                FROM proveedor
                WHERE ? = 1 OR estado = 'ACTIVO'
                ORDER BY nombre COLLATE NOCASE
                """;
        return jdbc.query(
                sql,
                statement -> statement.setInt(1, includeInactive ? 1 : 0),
                this::map,
                "No se pudieron listar los proveedores."
        );
    }

    @Override
    public Optional<Proveedor> findById(long id) {
        String sql = """
                SELECT id, nombre, telefono, whatsapp, direccion, observacion, estado
                FROM proveedor
                WHERE id = ?
                """;
        return jdbc.queryOne(
                sql,
                statement -> statement.setLong(1, id),
                this::map,
                "No se pudo leer el proveedor."
        );
    }

    @Override
    public Proveedor save(Proveedor proveedor) {
        if (proveedor.id() == null) {
            return insert(proveedor);
        }
        return update(proveedor);
    }

    @Override
    public void updateEstado(long id, EstadoProveedor estado) {
        String sql = "UPDATE proveedor SET estado = ?, updated_at = datetime('now') WHERE id = ?";
        jdbc.update(sql, statement -> {
            statement.setString(1, estado.dbValue());
            statement.setLong(2, id);
        }, "No se pudo cambiar el estado del proveedor.");
    }

    private Proveedor insert(Proveedor proveedor) {
        String sql = """
                INSERT INTO proveedor (nombre, telefono, whatsapp, direccion, observacion, estado, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, datetime('now'))
                """;
        long id = jdbc.insertReturningId(
                sql,
                statement -> bind(statement, proveedor, false),
                "No se pudo crear el proveedor."
        );
        return findById(id).orElseThrow(() -> new InfrastructureException("No se pudo recuperar el proveedor creado."));
    }

    private Proveedor update(Proveedor proveedor) {
        String sql = """
                UPDATE proveedor
                SET nombre = ?, telefono = ?, whatsapp = ?, direccion = ?, observacion = ?, estado = ?, updated_at = datetime('now')
                WHERE id = ?
                """;
        jdbc.update(
                sql,
                statement -> bind(statement, proveedor, true),
                "No se pudo actualizar el proveedor."
        );
        return findById(proveedor.id()).orElseThrow(() -> new InfrastructureException("No se pudo recuperar el proveedor actualizado."));
    }

    private void bind(PreparedStatement statement, Proveedor proveedor, boolean withId) throws SQLException {
        statement.setString(1, normalize(proveedor.nombre()));
        statement.setString(2, blankToNull(proveedor.telefono()));
        statement.setString(3, blankToNull(proveedor.whatsapp()));
        statement.setString(4, blankToNull(proveedor.direccion()));
        statement.setString(5, blankToNull(proveedor.observacion()));
        statement.setString(6, proveedor.estado().dbValue());
        if (withId) {
            statement.setLong(7, proveedor.id());
        }
    }

    private Proveedor map(ResultSet rs) throws SQLException {
        return new Proveedor(
                rs.getLong("id"),
                rs.getString("nombre"),
                emptyIfNull(rs.getString("telefono")),
                emptyIfNull(rs.getString("whatsapp")),
                emptyIfNull(rs.getString("direccion")),
                emptyIfNull(rs.getString("observacion")),
                EstadoProveedor.fromDb(rs.getString("estado"))
        );
    }
}
