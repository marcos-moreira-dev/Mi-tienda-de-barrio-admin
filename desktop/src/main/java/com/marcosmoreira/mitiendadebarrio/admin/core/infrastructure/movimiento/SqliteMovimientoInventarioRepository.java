package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.movimiento;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.movimiento.MovimientoInventarioRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.movimiento.MovimientoInventario;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.movimiento.TipoMovimientoInventario;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Adaptador SQLite para movimientos de inventario y ajustes manuales. */
public final class SqliteMovimientoInventarioRepository extends SqliteRepositorySupport implements MovimientoInventarioRepository {

    public SqliteMovimientoInventarioRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<MovimientoInventario> findRecent(String query, int limit) {
        String sql = """
                SELECT m.id, m.producto_id, p.nombre AS producto_nombre, m.lote_id, m.tipo_movimiento,
                       m.cantidad, m.stock_anterior, m.stock_nuevo, m.fecha_movimiento,
                       m.referencia_tipo, m.referencia_id, m.motivo, m.responsable_texto, m.observacion
                FROM movimiento_inventario m
                JOIN producto p ON p.id = m.producto_id
                WHERE (? IS NULL OR p.nombre LIKE ? OR m.tipo_movimiento LIKE ? OR m.motivo LIKE ?)
                ORDER BY m.fecha_movimiento DESC, m.id DESC
                LIMIT ?
                """;
        String normalized = query == null || query.isBlank() ? null : "%" + query.strip() + "%";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalized);
            statement.setString(2, normalized);
            statement.setString(3, normalized);
            statement.setString(4, normalized);
            statement.setInt(5, Math.max(1, limit));
            try (ResultSet rs = statement.executeQuery()) {
                List<MovimientoInventario> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(map(rs));
                }
                return items;
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudieron listar los movimientos de inventario.", ex);
        }
    }

    @Override
    public MovimientoInventario registrarAjuste(Long productoId, TipoMovimientoInventario tipo, BigDecimal cantidad, String motivo, String responsable, String observacion) {
        try (Connection connection = connectionFactory.openConnection()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal stockAnterior = leerStockActual(connection, productoId);
                BigDecimal stockNuevo = calcularStockNuevo(stockAnterior, tipo, cantidad);
                if (stockNuevo.compareTo(BigDecimal.ZERO) < 0) {
                    throw new ValidationException("El movimiento dejaría el stock en negativo.");
                }
                actualizarStock(connection, productoId, stockNuevo);
                long movimientoId = insertarMovimiento(connection, productoId, tipo, cantidad, stockAnterior, stockNuevo, motivo, responsable, observacion);
                if (tipo == TipoMovimientoInventario.MERMA || tipo == TipoMovimientoInventario.RETIRO_VENCIMIENTO) {
                    insertarMermaRetiro(connection, productoId, tipo, cantidad, stockAnterior, stockNuevo, observacion);
                }
                connection.commit();
                return findById(movimientoId);
            } catch (RuntimeException | SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (ValidationException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo registrar el ajuste de inventario.", ex);
        }
    }

    private BigDecimal leerStockActual(Connection connection, Long productoId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT stock_actual FROM producto WHERE id = ? AND estado = 'ACTIVO'")) {
            statement.setLong(1, productoId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new ValidationException("El producto seleccionado no existe o está inactivo.");
                }
                BigDecimal stock = rs.getBigDecimal("stock_actual");
                return stock == null ? BigDecimal.ZERO : stock;
            }
        }
    }

    private BigDecimal calcularStockNuevo(BigDecimal stockAnterior, TipoMovimientoInventario tipo, BigDecimal cantidad) {
        if (tipo == TipoMovimientoInventario.CORRECCION) {
            return cantidad;
        }
        return tipo.sign() >= 0 ? stockAnterior.add(cantidad) : stockAnterior.subtract(cantidad);
    }

    private void actualizarStock(Connection connection, Long productoId, BigDecimal stockNuevo) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE producto SET stock_actual = ?, updated_at = datetime('now') WHERE id = ?")) {
            statement.setBigDecimal(1, stockNuevo);
            statement.setLong(2, productoId);
            statement.executeUpdate();
        }
    }

    private long insertarMovimiento(Connection connection, Long productoId, TipoMovimientoInventario tipo, BigDecimal cantidad, BigDecimal stockAnterior, BigDecimal stockNuevo, String motivo, String responsable, String observacion) throws SQLException {
        String sql = """
                INSERT INTO movimiento_inventario (producto_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo,
                    referencia_tipo, motivo, responsable_texto, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, 'AJUSTE_MANUAL', ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, productoId);
            statement.setString(2, tipo.dbValue());
            statement.setBigDecimal(3, cantidad);
            statement.setBigDecimal(4, stockAnterior);
            statement.setBigDecimal(5, stockNuevo);
            statement.setString(6, blankToNull(motivo));
            statement.setString(7, blankToNull(responsable));
            statement.setString(8, blankToNull(observacion));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new InfrastructureException("No se pudo obtener el ID del movimiento creado.");
    }

    private void insertarMermaRetiro(Connection connection, Long productoId, TipoMovimientoInventario tipo, BigDecimal cantidad, BigDecimal stockAnterior, BigDecimal stockNuevo, String observacion) throws SQLException {
        String motivo = tipo == TipoMovimientoInventario.RETIRO_VENCIMIENTO ? "VENCIDO" : "DANADO";
        String sql = """
                INSERT INTO merma_retiro (producto_id, tipo_motivo, cantidad, stock_anterior, stock_nuevo, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productoId);
            statement.setString(2, motivo);
            statement.setBigDecimal(3, cantidad);
            statement.setBigDecimal(4, stockAnterior);
            statement.setBigDecimal(5, stockNuevo);
            statement.setString(6, blankToNull(observacion));
            statement.executeUpdate();
        }
    }

    private MovimientoInventario findById(long id) throws SQLException {
        String sql = """
                SELECT m.id, m.producto_id, p.nombre AS producto_nombre, m.lote_id, m.tipo_movimiento,
                       m.cantidad, m.stock_anterior, m.stock_nuevo, m.fecha_movimiento,
                       m.referencia_tipo, m.referencia_id, m.motivo, m.responsable_texto, m.observacion
                FROM movimiento_inventario m
                JOIN producto p ON p.id = m.producto_id
                WHERE m.id = ?
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        throw new InfrastructureException("No se pudo leer el movimiento creado.");
    }

    private MovimientoInventario map(ResultSet rs) throws SQLException {
        String fecha = rs.getString("fecha_movimiento");
        return new MovimientoInventario(
                rs.getLong("id"), rs.getLong("producto_id"), rs.getString("producto_nombre"), nullableLong(rs, "lote_id"),
                TipoMovimientoInventario.fromDb(rs.getString("tipo_movimiento")), rs.getBigDecimal("cantidad"),
                rs.getBigDecimal("stock_anterior"), rs.getBigDecimal("stock_nuevo"), fecha == null ? null : LocalDateTime.parse(fecha.replace(' ', 'T')),
                rs.getString("referencia_tipo"), nullableLong(rs, "referencia_id"), rs.getString("motivo"),
                rs.getString("responsable_texto"), rs.getString("observacion")
        );
    }
}
