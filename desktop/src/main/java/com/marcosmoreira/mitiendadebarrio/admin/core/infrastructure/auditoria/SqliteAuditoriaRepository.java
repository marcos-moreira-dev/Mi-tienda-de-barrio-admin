package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.auditoria;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.auditoria.AuditoriaRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.auditoria.AuditoriaEvento;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.auditoria.ResultadoAuditoria;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;

import java.util.List;

/** Adaptador SQLite para auditoría local. */
public final class SqliteAuditoriaRepository extends SqliteRepositorySupport implements AuditoriaRepository {
    public SqliteAuditoriaRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public AuditoriaEvento registrar(AuditoriaEvento evento) {
        String sql = """
                INSERT INTO auditoria_evento (usuario_id, modulo, accion, entidad, entidad_id, resumen, detalle_json, resultado)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        long id = jdbc.insertReturningId(sql, statement -> {
            setNullableLong(statement, 1, evento.usuarioId());
            statement.setString(2, evento.modulo());
            statement.setString(3, evento.accion());
            statement.setString(4, blankToNull(evento.entidad()));
            setNullableLong(statement, 5, evento.entidadId());
            statement.setString(6, evento.resumen());
            statement.setString(7, blankToNull(evento.detalleJson()));
            statement.setString(8, evento.resultado().name());
        }, "No se pudo registrar el evento de auditoría local.");
        return buscarPorId(id);
    }

    @Override
    public List<AuditoriaEvento> listarRecientes(int limite) {
        String sql = """
                SELECT id, usuario_id, fecha_evento, modulo, accion, entidad, entidad_id, resumen, detalle_json, resultado
                FROM auditoria_evento
                ORDER BY id DESC
                LIMIT ?
                """;
        int safeLimit = limite <= 0 ? 50 : Math.min(limite, 500);
        return jdbc.query(sql, statement -> statement.setInt(1, safeLimit), this::map,
                "No se pudieron consultar los eventos recientes de auditoría.");
    }

    private AuditoriaEvento buscarPorId(long id) {
        String sql = """
                SELECT id, usuario_id, fecha_evento, modulo, accion, entidad, entidad_id, resumen, detalle_json, resultado
                FROM auditoria_evento
                WHERE id = ?
                """;
        return jdbc.queryOne(sql, statement -> statement.setLong(1, id), this::map,
                "No se pudo consultar el evento de auditoría registrado.")
                .orElseThrow(() -> new IllegalStateException("No se encontró el evento de auditoría registrado."));
    }

    private AuditoriaEvento map(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        return new AuditoriaEvento(
                resultSet.getLong("id"),
                nullableLong(resultSet, "usuario_id"),
                resultSet.getString("fecha_evento"),
                resultSet.getString("modulo"),
                resultSet.getString("accion"),
                resultSet.getString("entidad"),
                nullableLong(resultSet, "entidad_id"),
                resultSet.getString("resumen"),
                resultSet.getString("detalle_json"),
                ResultadoAuditoria.fromDb(resultSet.getString("resultado"))
        );
    }
}
