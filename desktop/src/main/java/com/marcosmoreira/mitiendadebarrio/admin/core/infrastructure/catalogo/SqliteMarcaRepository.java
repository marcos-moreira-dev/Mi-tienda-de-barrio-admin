package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.catalogo;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.catalogo.MarcaRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.EstadoCatalogo;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.Marca;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Adaptador SQLite para marcas. */
public final class SqliteMarcaRepository extends SqliteRepositorySupport implements MarcaRepository {

    public SqliteMarcaRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<Marca> findAll(boolean includeInactive) {
        String sql = """
                SELECT id, nombre, descripcion, estado
                FROM marca
                WHERE ? = 1 OR estado = 'ACTIVA'
                ORDER BY nombre COLLATE NOCASE
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, includeInactive ? 1 : 0);
            try (ResultSet rs = statement.executeQuery()) {
                List<Marca> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(map(rs));
                }
                return items;
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudieron listar las marcas.", ex);
        }
    }

    @Override
    public Optional<Marca> findById(long id) {
        String sql = "SELECT id, nombre, descripcion, estado FROM marca WHERE id = ?";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo leer la marca.", ex);
        }
    }

    @Override
    public Marca save(Marca marca) {
        if (marca.id() == null) {
            return insert(marca);
        }
        return update(marca);
    }

    @Override
    public void updateEstado(long id, EstadoCatalogo estado) {
        String sql = "UPDATE marca SET estado = ?, updated_at = datetime('now') WHERE id = ?";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, estado.dbValue());
            statement.setLong(2, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo cambiar el estado de la marca.", ex);
        }
    }

    private Marca insert(Marca marca) {
        String sql = """
                INSERT INTO marca (nombre, descripcion, estado, updated_at)
                VALUES (?, ?, ?, datetime('now'))
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, normalize(marca.nombre()));
            statement.setString(2, blankToNull(marca.descripcion()));
            statement.setString(3, marca.estado().dbValue());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return findById(keys.getLong(1)).orElseThrow();
                }
                throw new InfrastructureException("No se pudo obtener el ID de la marca creada.");
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo crear la marca.", ex);
        }
    }

    private Marca update(Marca marca) {
        String sql = """
                UPDATE marca
                SET nombre = ?, descripcion = ?, estado = ?, updated_at = datetime('now')
                WHERE id = ?
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalize(marca.nombre()));
            statement.setString(2, blankToNull(marca.descripcion()));
            statement.setString(3, marca.estado().dbValue());
            statement.setLong(4, marca.id());
            statement.executeUpdate();
            return findById(marca.id()).orElseThrow();
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo actualizar la marca.", ex);
        }
    }

    private Marca map(ResultSet rs) throws SQLException {
        return new Marca(
                rs.getLong("id"),
                rs.getString("nombre"),
                emptyIfNull(rs.getString("descripcion")),
                EstadoCatalogo.fromDb(rs.getString("estado"))
        );
    }
}
