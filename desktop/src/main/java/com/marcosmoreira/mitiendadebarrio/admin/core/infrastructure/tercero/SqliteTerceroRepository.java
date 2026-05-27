package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.tercero;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.tercero.TerceroRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.tercero.EstadoTercero;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.tercero.Tercero;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.tercero.TipoTercero;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Adaptador SQLite para terceros locales. */
public final class SqliteTerceroRepository extends SqliteRepositorySupport implements TerceroRepository {

    public SqliteTerceroRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<Tercero> findAll(boolean includeInactive) {
        String sql = """
                SELECT t.id, t.tipo_tercero, t.tipo_identificacion, t.numero_identificacion,
                       t.nombre_legal, t.nombre_comercial, t.telefono, t.whatsapp, t.correo,
                       t.observacion, t.estado,
                       CASE WHEN cp.tercero_id IS NULL THEN 0 ELSE 1 END AS es_cliente,
                       CASE WHEN pp.tercero_id IS NULL THEN 0 ELSE 1 END AS es_proveedor
                FROM tercero t
                LEFT JOIN cliente_perfil cp ON cp.tercero_id = t.id AND cp.estado = 'ACTIVO'
                LEFT JOIN proveedor_perfil pp ON pp.tercero_id = t.id AND pp.estado = 'ACTIVO'
                WHERE ? = 1 OR t.estado = 'ACTIVO'
                ORDER BY COALESCE(NULLIF(t.nombre_comercial, ''), t.nombre_legal) COLLATE NOCASE
                """;
        return jdbc.query(
                sql,
                statement -> statement.setInt(1, includeInactive ? 1 : 0),
                this::map,
                "No se pudieron listar los clientes/proveedores."
        );
    }

    @Override
    public Optional<Tercero> findById(long id) {
        String sql = """
                SELECT t.id, t.tipo_tercero, t.tipo_identificacion, t.numero_identificacion,
                       t.nombre_legal, t.nombre_comercial, t.telefono, t.whatsapp, t.correo,
                       t.observacion, t.estado,
                       CASE WHEN cp.tercero_id IS NULL THEN 0 ELSE 1 END AS es_cliente,
                       CASE WHEN pp.tercero_id IS NULL THEN 0 ELSE 1 END AS es_proveedor
                FROM tercero t
                LEFT JOIN cliente_perfil cp ON cp.tercero_id = t.id AND cp.estado = 'ACTIVO'
                LEFT JOIN proveedor_perfil pp ON pp.tercero_id = t.id AND pp.estado = 'ACTIVO'
                WHERE t.id = ?
                """;
        return jdbc.queryOne(
                sql,
                statement -> statement.setLong(1, id),
                this::map,
                "No se pudo leer el cliente/proveedor."
        );
    }

    @Override
    public Tercero save(Tercero tercero) {
        if (tercero.id() == null) {
            return insert(tercero);
        }
        return update(tercero);
    }

    @Override
    public void updateEstado(long id, EstadoTercero estado) {
        String sql = "UPDATE tercero SET estado = ?, updated_at = datetime('now') WHERE id = ?";
        jdbc.update(sql, statement -> {
            statement.setString(1, estado.dbValue());
            statement.setLong(2, id);
        }, "No se pudo cambiar el estado del cliente/proveedor.");
    }

    @Override
    public void asegurarCliente(long terceroId, boolean permiteFiado) {
        String sql = """
                INSERT INTO cliente_perfil (tercero_id, permite_fiado, estado, updated_at)
                VALUES (?, ?, 'ACTIVO', datetime('now'))
                ON CONFLICT(tercero_id) DO UPDATE SET
                    permite_fiado = excluded.permite_fiado,
                    estado = 'ACTIVO',
                    updated_at = datetime('now')
                """;
        jdbc.update(sql, statement -> {
            statement.setLong(1, terceroId);
            statement.setInt(2, permiteFiado ? 1 : 0);
        }, "No se pudo activar el perfil de cliente.");
    }

    @Override
    public void asegurarProveedor(long terceroId) {
        String sql = """
                INSERT INTO proveedor_perfil (tercero_id, estado, updated_at)
                VALUES (?, 'ACTIVO', datetime('now'))
                ON CONFLICT(tercero_id) DO UPDATE SET
                    estado = 'ACTIVO',
                    updated_at = datetime('now')
                """;
        jdbc.update(sql, statement -> statement.setLong(1, terceroId), "No se pudo activar el perfil de proveedor.");
    }

    private Tercero insert(Tercero tercero) {
        String sql = """
                INSERT INTO tercero (
                    tipo_tercero, tipo_identificacion, numero_identificacion, nombre_legal,
                    nombre_comercial, telefono, whatsapp, correo, observacion, estado, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
                """;
        long id = jdbc.insertReturningId(
                sql,
                statement -> bind(statement, tercero, false),
                "No se pudo crear el cliente/proveedor."
        );
        return findById(id).orElseThrow(() -> new InfrastructureException("No se pudo recuperar el cliente/proveedor creado."));
    }

    private Tercero update(Tercero tercero) {
        String sql = """
                UPDATE tercero
                SET tipo_tercero = ?, tipo_identificacion = ?, numero_identificacion = ?,
                    nombre_legal = ?, nombre_comercial = ?, telefono = ?, whatsapp = ?,
                    correo = ?, observacion = ?, estado = ?, updated_at = datetime('now')
                WHERE id = ?
                """;
        jdbc.update(
                sql,
                statement -> bind(statement, tercero, true),
                "No se pudo actualizar el cliente/proveedor."
        );
        return findById(tercero.id()).orElseThrow(() -> new InfrastructureException("No se pudo recuperar el cliente/proveedor actualizado."));
    }

    private void bind(PreparedStatement statement, Tercero tercero, boolean withId) throws SQLException {
        statement.setString(1, (tercero.tipo() == null ? TipoTercero.PERSONA_NATURAL : tercero.tipo()).dbValue());
        statement.setString(2, blankToNull(tercero.tipoIdentificacion()));
        statement.setString(3, blankToNull(tercero.numeroIdentificacion()));
        statement.setString(4, blankToNull(tercero.nombreLegal()));
        statement.setString(5, blankToNull(tercero.nombreComercial()));
        statement.setString(6, blankToNull(tercero.telefono()));
        statement.setString(7, blankToNull(tercero.whatsapp()));
        statement.setString(8, blankToNull(tercero.correo()));
        statement.setString(9, blankToNull(tercero.observacion()));
        statement.setString(10, (tercero.estado() == null ? EstadoTercero.ACTIVO : tercero.estado()).dbValue());
        if (withId) {
            statement.setLong(11, tercero.id());
        }
    }

    private Tercero map(ResultSet rs) throws SQLException {
        return new Tercero(
                rs.getLong("id"),
                TipoTercero.fromDb(rs.getString("tipo_tercero")),
                emptyIfNull(rs.getString("tipo_identificacion")),
                emptyIfNull(rs.getString("numero_identificacion")),
                emptyIfNull(rs.getString("nombre_legal")),
                emptyIfNull(rs.getString("nombre_comercial")),
                emptyIfNull(rs.getString("telefono")),
                emptyIfNull(rs.getString("whatsapp")),
                emptyIfNull(rs.getString("correo")),
                emptyIfNull(rs.getString("observacion")),
                EstadoTercero.fromDb(rs.getString("estado")),
                rs.getInt("es_cliente") == 1,
                rs.getInt("es_proveedor") == 1
        );
    }
}
