package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.dashboard;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.dashboard.DashboardRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.dashboard.DashboardResumen;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/** Consultas agregadas para el tablero operativo local. */
public final class SqliteDashboardRepository extends SqliteRepositorySupport implements DashboardRepository {

    public SqliteDashboardRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public DashboardResumen resumenBase(String estadoLicencia) {
        try (Connection connection = connectionFactory.openConnection()) {
            long productosActivos = count(connection, "SELECT COUNT(*) FROM producto WHERE estado = 'ACTIVO'");
            long bajoStock = count(connection, "SELECT COUNT(*) FROM producto WHERE estado = 'ACTIVO' AND stock_actual <= stock_minimo AND stock_actual > 0");
            long agotados = count(connection, "SELECT COUNT(*) FROM producto WHERE estado = 'ACTIVO' AND stock_actual = 0");
            long porComprar = count(connection, "SELECT COUNT(*) FROM producto WHERE estado = 'ACTIVO' AND stock_actual <= stock_minimo");
            long proximosVencer = count(connection, "SELECT COUNT(*) FROM lote_producto WHERE estado = 'DISPONIBLE' AND fecha_vencimiento IS NOT NULL AND date(fecha_vencimiento) <= date('now', '+30 day')");
            BigDecimal ventasHoy = money(connection, "SELECT COALESCE(SUM(total), 0) FROM venta_interna WHERE estado = 'REGISTRADA' AND date(fecha_venta) = date('now')");
            BigDecimal comprasHoy = money(connection, "SELECT COALESCE(SUM(total_estimado), 0) FROM compra WHERE estado = 'REGISTRADA' AND date(fecha_compra) = date('now')");
            boolean cajaAbierta = count(connection, "SELECT COUNT(*) FROM caja_diaria WHERE fecha = date('now') AND estado = 'ABIERTA'") > 0;
            LocalDateTime ultimoRespaldo = dateTime(connection, "SELECT MAX(fecha_respaldo) FROM respaldo_sistema WHERE estado = 'CREADO'");
            return new DashboardResumen(productosActivos, bajoStock, agotados, porComprar, proximosVencer, ventasHoy, comprasHoy, cajaAbierta, ultimoRespaldo, estadoLicencia);
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo generar el resumen del tablero.", ex);
        }
    }

    private long count(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    private BigDecimal money(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            if (!rs.next()) return BigDecimal.ZERO;
            BigDecimal value = rs.getBigDecimal(1);
            return value == null ? BigDecimal.ZERO : value;
        }
    }

    private LocalDateTime dateTime(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            if (!rs.next()) return null;
            String value = rs.getString(1);
            return value == null || value.isBlank() ? null : LocalDateTime.parse(value.replace(' ', 'T'));
        }
    }
}
