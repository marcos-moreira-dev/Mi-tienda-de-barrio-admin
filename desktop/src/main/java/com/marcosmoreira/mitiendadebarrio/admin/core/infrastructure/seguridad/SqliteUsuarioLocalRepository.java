package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.seguridad;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.seguridad.UsuarioLocalRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.seguridad.EstadoUsuarioLocal;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.seguridad.UsuarioLocalCredenciales;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;

import java.util.List;
import java.util.Optional;

/** Adaptador SQLite para cuentas locales, roles y permisos. */
public final class SqliteUsuarioLocalRepository extends SqliteRepositorySupport implements UsuarioLocalRepository {
    public SqliteUsuarioLocalRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public Optional<UsuarioLocalCredenciales> buscarCredencialesPorUsuario(String nombreUsuario) {
        String sql = """
                SELECT id, nombre_usuario, nombre_visible, password_hash, password_salt, algoritmo_hash,
                       estado, debe_cambiar_clave
                FROM usuario_local
                WHERE lower(nombre_usuario) = lower(?)
                LIMIT 1
                """;
        return jdbc.queryOne(sql, statement -> statement.setString(1, nombreUsuario), resultSet -> new UsuarioLocalCredenciales(
                resultSet.getLong("id"),
                resultSet.getString("nombre_usuario"),
                resultSet.getString("nombre_visible"),
                resultSet.getString("password_hash"),
                resultSet.getString("password_salt"),
                resultSet.getString("algoritmo_hash"),
                EstadoUsuarioLocal.fromDb(resultSet.getString("estado")),
                resultSet.getInt("debe_cambiar_clave") == 1
        ), "No se pudo consultar el usuario local.");
    }

    @Override
    public List<String> rolesDeUsuario(long usuarioId) {
        String sql = """
                SELECT r.codigo
                FROM rol_local r
                INNER JOIN usuario_rol_local ur ON ur.rol_id = r.id
                WHERE ur.usuario_id = ? AND r.estado = 'ACTIVO'
                ORDER BY r.codigo
                """;
        return jdbc.query(sql, statement -> statement.setLong(1, usuarioId), resultSet -> resultSet.getString("codigo"),
                "No se pudieron consultar los roles del usuario local.");
    }

    @Override
    public void actualizarUltimoAcceso(long usuarioId) {
        String sql = "UPDATE usuario_local SET ultimo_acceso = datetime('now'), updated_at = datetime('now') WHERE id = ?";
        jdbc.update(sql, statement -> statement.setLong(1, usuarioId), "No se pudo actualizar el último acceso del usuario local.");
    }
}
