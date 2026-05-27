package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.configuracion;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.configuracion.ConfiguracionNegocioRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.configuracion.ConfiguracionNegocio;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Adaptador SQLite para la configuración única del negocio. */
public final class SqliteConfiguracionNegocioRepository extends SqliteRepositorySupport implements ConfiguracionNegocioRepository {

    public SqliteConfiguracionNegocioRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public Optional<ConfiguracionNegocio> findCurrent() {
        String sql = """
                SELECT nombre_comercial, ruc, responsable, telefono, direccion, actividad, moneda, observacion
                FROM configuracion_negocio
                WHERE id = 1
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                return Optional.empty();
            }
            return Optional.of(new ConfiguracionNegocio(
                    text(resultSet, "nombre_comercial"), text(resultSet, "ruc"),
                    text(resultSet, "responsable"), text(resultSet, "telefono"),
                    text(resultSet, "direccion"), text(resultSet, "actividad"),
                    text(resultSet, "moneda"), text(resultSet, "observacion")
            ));
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo leer la configuración del negocio.", ex);
        }
    }

    @Override
    public void save(ConfiguracionNegocio configuracion) {
        String sql = """
                INSERT INTO configuracion_negocio
                (id, nombre_comercial, ruc, responsable, telefono, direccion, actividad, moneda, observacion, updated_at)
                VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
                ON CONFLICT(id) DO UPDATE SET
                    nombre_comercial = excluded.nombre_comercial,
                    ruc = excluded.ruc,
                    responsable = excluded.responsable,
                    telefono = excluded.telefono,
                    direccion = excluded.direccion,
                    actividad = excluded.actividad,
                    moneda = excluded.moneda,
                    observacion = excluded.observacion,
                    updated_at = datetime('now')
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, blankToNull(configuracion.nombreComercial()));
            statement.setString(2, blankToNull(configuracion.ruc()));
            statement.setString(3, blankToNull(configuracion.responsable()));
            statement.setString(4, blankToNull(configuracion.telefono()));
            statement.setString(5, blankToNull(configuracion.direccion()));
            statement.setString(6, blankToNull(configuracion.actividad()));
            statement.setString(7, blankToNull(configuracion.moneda()));
            statement.setString(8, blankToNull(configuracion.observacion()));
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo guardar la configuración del negocio.", ex);
        }
    }

    private String text(ResultSet resultSet, String column) throws SQLException {
        String value = resultSet.getString(column);
        return value == null ? "" : value;
    }
}
