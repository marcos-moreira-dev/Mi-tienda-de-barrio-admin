package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.catalogo;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.catalogo.UnidadMedidaRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.EstadoCatalogo;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.UnidadMedida;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Adaptador SQLite para unidades de medida. */
public final class SqliteUnidadMedidaRepository extends SqliteRepositorySupport implements UnidadMedidaRepository {

    public SqliteUnidadMedidaRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<UnidadMedida> findAll(boolean includeInactive) {
        String sql = """
                SELECT id, nombre, abreviatura, permite_decimales, estado
                FROM unidad_medida
                WHERE ? = 1 OR estado = 'ACTIVA'
                ORDER BY nombre COLLATE NOCASE
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, includeInactive ? 1 : 0);
            try (ResultSet rs = statement.executeQuery()) {
                List<UnidadMedida> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(map(rs));
                }
                return items;
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudieron listar las unidades de medida.", ex);
        }
    }

    @Override
    public Optional<UnidadMedida> findById(long id) {
        String sql = "SELECT id, nombre, abreviatura, permite_decimales, estado FROM unidad_medida WHERE id = ?";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo leer la unidad de medida.", ex);
        }
    }

    @Override
    public UnidadMedida save(UnidadMedida unidadMedida) {
        if (unidadMedida.id() == null) {
            return insert(unidadMedida);
        }
        return update(unidadMedida);
    }

    @Override
    public void updateEstado(long id, EstadoCatalogo estado) {
        String sql = "UPDATE unidad_medida SET estado = ?, updated_at = datetime('now') WHERE id = ?";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, estado.dbValue());
            statement.setLong(2, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo cambiar el estado de la unidad de medida.", ex);
        }
    }

    private UnidadMedida insert(UnidadMedida unidadMedida) {
        String sql = """
                INSERT INTO unidad_medida (nombre, abreviatura, permite_decimales, estado, updated_at)
                VALUES (?, ?, ?, ?, datetime('now'))
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, normalize(unidadMedida.nombre()));
            statement.setString(2, normalize(unidadMedida.abreviatura()));
            statement.setInt(3, unidadMedida.permiteDecimales() ? 1 : 0);
            statement.setString(4, unidadMedida.estado().dbValue());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return findById(keys.getLong(1)).orElseThrow();
                }
                throw new InfrastructureException("No se pudo obtener el ID de la unidad de medida creada.");
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo crear la unidad de medida.", ex);
        }
    }

    private UnidadMedida update(UnidadMedida unidadMedida) {
        String sql = """
                UPDATE unidad_medida
                SET nombre = ?, abreviatura = ?, permite_decimales = ?, estado = ?, updated_at = datetime('now')
                WHERE id = ?
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalize(unidadMedida.nombre()));
            statement.setString(2, normalize(unidadMedida.abreviatura()));
            statement.setInt(3, unidadMedida.permiteDecimales() ? 1 : 0);
            statement.setString(4, unidadMedida.estado().dbValue());
            statement.setLong(5, unidadMedida.id());
            statement.executeUpdate();
            return findById(unidadMedida.id()).orElseThrow();
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo actualizar la unidad de medida.", ex);
        }
    }

    private UnidadMedida map(ResultSet rs) throws SQLException {
        return new UnidadMedida(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getString("abreviatura"),
                rs.getInt("permite_decimales") == 1,
                EstadoCatalogo.fromDb(rs.getString("estado"))
        );
    }
}
