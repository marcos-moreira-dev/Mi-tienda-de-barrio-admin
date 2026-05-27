package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.catalogo;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.catalogo.CategoriaRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.Categoria;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.EstadoCatalogo;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Adaptador SQLite para categorías. */
public final class SqliteCategoriaRepository extends SqliteRepositorySupport implements CategoriaRepository {

    public SqliteCategoriaRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<Categoria> findAll(boolean includeInactive) {
        String sql = """
                SELECT id, nombre, descripcion, estado
                FROM categoria
                WHERE ? = 1 OR estado = 'ACTIVA'
                ORDER BY nombre COLLATE NOCASE
                """;
        return jdbc.query(
                sql,
                statement -> statement.setInt(1, includeInactive ? 1 : 0),
                this::map,
                "No se pudieron listar las categorías."
        );
    }

    @Override
    public Optional<Categoria> findById(long id) {
        String sql = "SELECT id, nombre, descripcion, estado FROM categoria WHERE id = ?";
        return jdbc.queryOne(
                sql,
                statement -> statement.setLong(1, id),
                this::map,
                "No se pudo leer la categoría."
        );
    }

    @Override
    public Categoria save(Categoria categoria) {
        if (categoria.id() == null) {
            return insert(categoria);
        }
        return update(categoria);
    }

    @Override
    public void updateEstado(long id, EstadoCatalogo estado) {
        String sql = "UPDATE categoria SET estado = ?, updated_at = datetime('now') WHERE id = ?";
        jdbc.update(sql, statement -> {
            statement.setString(1, estado.dbValue());
            statement.setLong(2, id);
        }, "No se pudo cambiar el estado de la categoría.");
    }

    private Categoria insert(Categoria categoria) {
        String sql = """
                INSERT INTO categoria (nombre, descripcion, estado, updated_at)
                VALUES (?, ?, ?, datetime('now'))
                """;
        long id = jdbc.insertReturningId(
                sql,
                statement -> bind(statement, categoria, false),
                "No se pudo crear la categoría."
        );
        return findById(id).orElseThrow(() -> new InfrastructureException("No se pudo recuperar la categoría creada."));
    }

    private Categoria update(Categoria categoria) {
        String sql = """
                UPDATE categoria
                SET nombre = ?, descripcion = ?, estado = ?, updated_at = datetime('now')
                WHERE id = ?
                """;
        jdbc.update(
                sql,
                statement -> bind(statement, categoria, true),
                "No se pudo actualizar la categoría."
        );
        return findById(categoria.id()).orElseThrow(() -> new InfrastructureException("No se pudo recuperar la categoría actualizada."));
    }

    private void bind(PreparedStatement statement, Categoria categoria, boolean withId) throws SQLException {
        statement.setString(1, normalize(categoria.nombre()));
        statement.setString(2, blankToNull(categoria.descripcion()));
        statement.setString(3, categoria.estado().dbValue());
        if (withId) {
            statement.setLong(4, categoria.id());
        }
    }

    private Categoria map(ResultSet rs) throws SQLException {
        return new Categoria(
                rs.getLong("id"),
                rs.getString("nombre"),
                emptyIfNull(rs.getString("descripcion")),
                EstadoCatalogo.fromDb(rs.getString("estado"))
        );
    }
}
