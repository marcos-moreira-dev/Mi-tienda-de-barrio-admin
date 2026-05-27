package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.inventario;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.inventario.InventarioFuerteRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.inventario.*;
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

/** Adaptador SQLite para conteos físicos y ajustes formales de inventario. */
public final class SqliteInventarioFuerteRepository extends SqliteRepositorySupport implements InventarioFuerteRepository {

    public SqliteInventarioFuerteRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<TipoMovimientoInventarioCatalogo> listarTiposMovimiento() {
        String sql = """
                SELECT codigo, nombre, signo, afecta_stock, reservado_sistema, estado
                FROM tipo_movimiento_inventario
                WHERE estado = 'ACTIVO'
                ORDER BY codigo
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<TipoMovimientoInventarioCatalogo> items = new ArrayList<>();
            while (rs.next()) {
                items.add(new TipoMovimientoInventarioCatalogo(
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getInt("signo"),
                        rs.getInt("afecta_stock") == 1,
                        rs.getInt("reservado_sistema") == 1,
                        rs.getString("estado")
                ));
            }
            return items;
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudieron leer los tipos de movimiento de inventario.", ex);
        }
    }

    @Override
    public ConteoInventario crearConteo(String responsableTexto, String observacion) {
        String sql = """
                INSERT INTO conteo_inventario (responsable_texto, observacion, updated_at)
                VALUES (?, ?, datetime('now'))
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, blankToNull(responsableTexto));
            statement.setString(2, blankToNull(observacion));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return leerConteo(connection, keys.getLong(1));
                }
            }
            throw new InfrastructureException("No se pudo obtener el ID del conteo creado.");
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo crear el conteo de inventario.", ex);
        }
    }

    @Override
    public ConteoInventarioDetalle registrarDetalleConteo(Long conteoId, Long productoId, BigDecimal stockContado, String observacion) {
        return transactions.inTransaction(connection -> {
            validarConteoAbierto(connection, conteoId);
            ProductoStock producto = leerProductoStock(connection, productoId);
            BigDecimal diferencia = stockContado.subtract(producto.stockActual());
            String sql = """
                    INSERT INTO conteo_inventario_detalle (conteo_id, producto_id, stock_sistema, stock_contado, diferencia, observacion, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, datetime('now'))
                    ON CONFLICT(conteo_id, producto_id) DO UPDATE SET
                        stock_sistema = excluded.stock_sistema,
                        stock_contado = excluded.stock_contado,
                        diferencia = excluded.diferencia,
                        observacion = excluded.observacion,
                        updated_at = datetime('now')
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, conteoId);
                statement.setLong(2, productoId);
                statement.setBigDecimal(3, producto.stockActual());
                statement.setBigDecimal(4, stockContado);
                statement.setBigDecimal(5, diferencia);
                statement.setString(6, blankToNull(observacion));
                statement.executeUpdate();
            }
            return leerDetalleConteo(connection, conteoId, productoId);
        }, "No se pudo registrar la línea del conteo.");
    }

    @Override
    public List<ConteoInventarioDetalle> listarDetallesConteo(Long conteoId) {
        String sql = """
                SELECT d.id, d.conteo_id, d.producto_id, p.nombre AS producto_nombre,
                       d.stock_sistema, d.stock_contado, d.diferencia, d.observacion
                FROM conteo_inventario_detalle d
                JOIN producto p ON p.id = d.producto_id
                WHERE d.conteo_id = ?
                ORDER BY p.nombre
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, conteoId);
            try (ResultSet rs = statement.executeQuery()) {
                List<ConteoInventarioDetalle> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(mapDetalleConteo(rs));
                }
                return items;
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudieron listar las líneas del conteo.", ex);
        }
    }

    @Override
    public ConteoInventario cerrarConteo(Long conteoId) {
        return transactions.inTransaction(connection -> {
            validarConteoAbierto(connection, conteoId);
            try (PreparedStatement statement = connection.prepareStatement("UPDATE conteo_inventario SET estado = 'CERRADO', updated_at = datetime('now') WHERE id = ?")) {
                statement.setLong(1, conteoId);
                statement.executeUpdate();
            }
            return leerConteo(connection, conteoId);
        }, "No se pudo cerrar el conteo de inventario.");
    }

    @Override
    public AjusteInventarioResultado registrarAjuste(AjusteInventarioSolicitud solicitud) {
        return transactions.inTransaction(connection -> {
            if (solicitud.conteoInventarioId() != null) {
                validarConteoExiste(connection, solicitud.conteoInventarioId());
            }
            ProductoStock producto = leerProductoStock(connection, solicitud.productoId());
            BigDecimal stockNuevo = calcularStockNuevo(producto.stockActual(), solicitud.tipoMovimiento(), solicitud.cantidad());
            if (stockNuevo.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("El ajuste dejaría el stock en negativo.");
            }
            long ajusteId = insertarAjuste(connection, solicitud);
            actualizarStock(connection, solicitud.productoId(), stockNuevo);
            long movimientoId = insertarMovimiento(connection, solicitud, producto.stockActual(), stockNuevo);
            insertarDetalleAjuste(connection, ajusteId, solicitud, producto.stockActual(), stockNuevo, movimientoId);
            insertarMermaSiAplica(connection, solicitud, producto.stockActual(), stockNuevo);
            return new AjusteInventarioResultado(ajusteId, movimientoId, solicitud.productoId(), producto.stockActual(), stockNuevo);
        }, "No se pudo registrar el ajuste formal de inventario.");
    }

    private BigDecimal calcularStockNuevo(BigDecimal stockAnterior, TipoMovimientoInventario tipo, BigDecimal cantidad) {
        if (tipo == TipoMovimientoInventario.CORRECCION) {
            return cantidad;
        }
        return tipo.sign() >= 0 ? stockAnterior.add(cantidad) : stockAnterior.subtract(cantidad);
    }

    private long insertarAjuste(Connection connection, AjusteInventarioSolicitud solicitud) throws SQLException {
        String sql = """
                INSERT INTO ajuste_inventario (conteo_inventario_id, responsable_texto, motivo, observacion, updated_at)
                VALUES (?, ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setNullableLong(statement, 1, solicitud.conteoInventarioId());
            statement.setString(2, blankToNull(solicitud.responsableTexto()));
            statement.setString(3, blankToNull(solicitud.motivo()));
            statement.setString(4, blankToNull(solicitud.observacion()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        throw new InfrastructureException("No se pudo obtener el ID del ajuste creado.");
    }

    private long insertarMovimiento(Connection connection, AjusteInventarioSolicitud solicitud, BigDecimal stockAnterior, BigDecimal stockNuevo) throws SQLException {
        String sql = """
                INSERT INTO movimiento_inventario (producto_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo,
                    referencia_tipo, motivo, responsable_texto, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, 'AJUSTE_INVENTARIO', ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, solicitud.productoId());
            statement.setString(2, solicitud.tipoMovimiento().dbValue());
            statement.setBigDecimal(3, solicitud.cantidad());
            statement.setBigDecimal(4, stockAnterior);
            statement.setBigDecimal(5, stockNuevo);
            statement.setString(6, blankToNull(solicitud.motivo()));
            statement.setString(7, blankToNull(solicitud.responsableTexto()));
            statement.setString(8, blankToNull(solicitud.observacion()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        throw new InfrastructureException("No se pudo obtener el ID del movimiento creado.");
    }

    private void insertarDetalleAjuste(Connection connection, long ajusteId, AjusteInventarioSolicitud solicitud, BigDecimal stockAnterior, BigDecimal stockNuevo, long movimientoId) throws SQLException {
        String sql = """
                INSERT INTO ajuste_inventario_detalle (ajuste_inventario_id, producto_id, tipo_movimiento, cantidad,
                    stock_anterior, stock_nuevo, movimiento_inventario_id, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, ajusteId);
            statement.setLong(2, solicitud.productoId());
            statement.setString(3, solicitud.tipoMovimiento().dbValue());
            statement.setBigDecimal(4, solicitud.cantidad());
            statement.setBigDecimal(5, stockAnterior);
            statement.setBigDecimal(6, stockNuevo);
            statement.setLong(7, movimientoId);
            statement.setString(8, blankToNull(solicitud.observacion()));
            statement.executeUpdate();
        }
    }

    private void insertarMermaSiAplica(Connection connection, AjusteInventarioSolicitud solicitud, BigDecimal stockAnterior, BigDecimal stockNuevo) throws SQLException {
        if (solicitud.tipoMovimiento() != TipoMovimientoInventario.MERMA && solicitud.tipoMovimiento() != TipoMovimientoInventario.RETIRO_VENCIMIENTO) {
            return;
        }
        String motivo = solicitud.tipoMovimiento() == TipoMovimientoInventario.RETIRO_VENCIMIENTO ? "VENCIDO" : "DANADO";
        String sql = """
                INSERT INTO merma_retiro (producto_id, tipo_motivo, cantidad, stock_anterior, stock_nuevo, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, solicitud.productoId());
            statement.setString(2, motivo);
            statement.setBigDecimal(3, solicitud.cantidad());
            statement.setBigDecimal(4, stockAnterior);
            statement.setBigDecimal(5, stockNuevo);
            statement.setString(6, blankToNull(solicitud.observacion()));
            statement.executeUpdate();
        }
    }

    private void actualizarStock(Connection connection, Long productoId, BigDecimal stockNuevo) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE producto SET stock_actual = ?, updated_at = datetime('now') WHERE id = ?")) {
            statement.setBigDecimal(1, stockNuevo);
            statement.setLong(2, productoId);
            statement.executeUpdate();
        }
    }

    private ProductoStock leerProductoStock(Connection connection, Long productoId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id, nombre, stock_actual FROM producto WHERE id = ? AND estado = 'ACTIVO'")) {
            statement.setLong(1, productoId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new ValidationException("El producto seleccionado no existe o está inactivo.");
                }
                return new ProductoStock(rs.getLong("id"), rs.getString("nombre"), zeroIfNull(rs.getBigDecimal("stock_actual")));
            }
        }
    }

    private void validarConteoAbierto(Connection connection, Long conteoId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT estado FROM conteo_inventario WHERE id = ?")) {
            statement.setLong(1, conteoId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) throw new ValidationException("El conteo seleccionado no existe.");
                if (!"ABIERTO".equals(rs.getString("estado"))) throw new ValidationException("Solo se puede modificar un conteo abierto.");
            }
        }
    }

    private void validarConteoExiste(Connection connection, Long conteoId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM conteo_inventario WHERE id = ?")) {
            statement.setLong(1, conteoId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) throw new ValidationException("El conteo asociado no existe.");
            }
        }
    }

    private ConteoInventario leerConteo(Connection connection, Long conteoId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id, fecha_conteo, estado, responsable_texto, observacion FROM conteo_inventario WHERE id = ?")) {
            statement.setLong(1, conteoId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return mapConteo(rs);
            }
        }
        throw new ValidationException("El conteo seleccionado no existe.");
    }

    private ConteoInventarioDetalle leerDetalleConteo(Connection connection, Long conteoId, Long productoId) throws SQLException {
        String sql = """
                SELECT d.id, d.conteo_id, d.producto_id, p.nombre AS producto_nombre,
                       d.stock_sistema, d.stock_contado, d.diferencia, d.observacion
                FROM conteo_inventario_detalle d
                JOIN producto p ON p.id = d.producto_id
                WHERE d.conteo_id = ? AND d.producto_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, conteoId);
            statement.setLong(2, productoId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return mapDetalleConteo(rs);
            }
        }
        throw new InfrastructureException("No se pudo leer la línea de conteo registrada.");
    }

    private ConteoInventario mapConteo(ResultSet rs) throws SQLException {
        String fecha = rs.getString("fecha_conteo");
        return new ConteoInventario(
                rs.getLong("id"),
                fecha == null ? null : LocalDateTime.parse(fecha.replace(' ', 'T')),
                EstadoConteoInventario.fromDb(rs.getString("estado")),
                rs.getString("responsable_texto"),
                rs.getString("observacion")
        );
    }

    private ConteoInventarioDetalle mapDetalleConteo(ResultSet rs) throws SQLException {
        return new ConteoInventarioDetalle(
                rs.getLong("id"),
                rs.getLong("conteo_id"),
                rs.getLong("producto_id"),
                rs.getString("producto_nombre"),
                zeroIfNull(rs.getBigDecimal("stock_sistema")),
                zeroIfNull(rs.getBigDecimal("stock_contado")),
                zeroIfNull(rs.getBigDecimal("diferencia")),
                rs.getString("observacion")
        );
    }

    private record ProductoStock(Long id, String nombre, BigDecimal stockActual) {}
}
