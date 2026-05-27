package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.respaldo;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.respaldo.RespaldoRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.respaldo.RespaldoSistema;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.respaldo.TipoRespaldo;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Persistencia de metadatos de respaldos. */
public final class SqliteRespaldoRepository extends SqliteRepositorySupport implements RespaldoRepository {

    public SqliteRespaldoRepository(SqliteConnectionFactory connectionFactory) { super(connectionFactory); }

    @Override
    public RespaldoSistema registrar(Path archivo, TipoRespaldo tipo, long pesoBytes, String hashSha256, String observacion) {
        String sql = """
                INSERT INTO respaldo_sistema (ruta_archivo, tipo_respaldo, estado, peso_bytes, hash_sha256, observacion, updated_at)
                VALUES (?, ?, 'CREADO', ?, ?, ?, datetime('now'))
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, archivo.toAbsolutePath().toString());
            statement.setString(2, tipo.name());
            statement.setLong(3, pesoBytes);
            statement.setString(4, hashSha256);
            statement.setString(5, observacion);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return listarPorId(keys.getLong(1));
            }
            throw new InfrastructureException("No se pudo obtener el ID del respaldo registrado.");
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo registrar el respaldo.", ex);
        }
    }


    @Override
    public void marcarRestaurado(Path archivo, String observacion) {
        String sql = """
                UPDATE respaldo_sistema
                SET estado = 'RESTAURADO', observacion = COALESCE(?, observacion), updated_at = datetime('now')
                WHERE ruta_archivo = ?
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, observacion);
            statement.setString(2, archivo.toAbsolutePath().toString());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo marcar el respaldo como restaurado.", ex);
        }
    }

    @Override
    public List<RespaldoSistema> listarRecientes(int limit) {
        String sql = """
                SELECT id, fecha_respaldo, ruta_archivo, tipo_respaldo, estado, peso_bytes, hash_sha256, observacion
                FROM respaldo_sistema
                ORDER BY fecha_respaldo DESC, id DESC
                LIMIT ?
                """;
        try (Connection connection = connectionFactory.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Math.max(1, limit));
            try (ResultSet rs = statement.executeQuery()) {
                List<RespaldoSistema> items = new ArrayList<>();
                while (rs.next()) items.add(map(rs));
                return items;
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudieron listar respaldos.", ex);
        }
    }

    private RespaldoSistema listarPorId(long id) throws SQLException {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, fecha_respaldo, ruta_archivo, tipo_respaldo, estado, peso_bytes, hash_sha256, observacion FROM respaldo_sistema WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) { if (rs.next()) return map(rs); }
        }
        throw new InfrastructureException("No se pudo leer el respaldo creado.");
    }

    private RespaldoSistema map(ResultSet rs) throws SQLException {
        String fecha = rs.getString("fecha_respaldo");
        return new RespaldoSistema(rs.getLong("id"), fecha == null ? null : LocalDateTime.parse(fecha.replace(' ', 'T')),
                Path.of(rs.getString("ruta_archivo")), TipoRespaldo.valueOf(rs.getString("tipo_respaldo")), rs.getString("estado"),
                nullableLong(rs, "peso_bytes"), rs.getString("hash_sha256"), rs.getString("observacion"));
    }
}
