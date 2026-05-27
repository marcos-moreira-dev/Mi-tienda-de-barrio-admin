package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.venta;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.venta.VentaInternaRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.AnulacionVentaInterna;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.DetalleVentaInternaAvanzada;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.MetodoPagoVentaInterna;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.RegistroAnulacionVentaInterna;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.RegistroVentaInternaAvanzada;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.RegistroVentaInternaSimple;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.VentaInterna;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Adaptador SQLite para ventas internas y salidas operativas. */
public final class SqliteVentaInternaRepository extends SqliteRepositorySupport implements VentaInternaRepository {

    public SqliteVentaInternaRepository(SqliteConnectionFactory connectionFactory) { super(connectionFactory); }

    @Override
    public List<VentaInterna> findRecent(String query, int limit) {
        String sql = """
                SELECT id, fecha_venta, total, metodo_pago, numero_referencia, estado, advertencia_tributaria_aceptada, observacion
                FROM venta_interna
                WHERE (? IS NULL OR numero_referencia LIKE ? OR metodo_pago LIKE ? OR observacion LIKE ?)
                ORDER BY fecha_venta DESC, id DESC
                LIMIT ?
                """;
        String normalized = query == null || query.isBlank() ? null : "%" + query.strip() + "%";
        try (Connection connection = connectionFactory.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalized);
            statement.setString(2, normalized);
            statement.setString(3, normalized);
            statement.setString(4, normalized);
            statement.setInt(5, Math.max(1, limit));
            try (ResultSet rs = statement.executeQuery()) {
                List<VentaInterna> items = new ArrayList<>();
                while (rs.next()) items.add(map(rs));
                return items;
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudieron listar las ventas internas.", ex);
        }
    }

    @Override
    public VentaInterna registrarVentaSimple(RegistroVentaInternaSimple command) {
        DetalleVentaInternaAvanzada detalle = new DetalleVentaInternaAvanzada(command.productoId(), command.cantidad(), command.precioUnitario(), command.observacion());
        RegistroVentaInternaAvanzada avanzada = new RegistroVentaInternaAvanzada(null, List.of(detalle),
                command.metodoPago() == null ? MetodoPagoVentaInterna.EFECTIVO : command.metodoPago(),
                command.numeroReferencia(), command.advertenciaTributariaAceptada(), command.observacion());
        return registrarVentaAvanzada(avanzada);
    }

    @Override
    public VentaInterna registrarVentaAvanzada(RegistroVentaInternaAvanzada command) {
        try (Connection connection = connectionFactory.openConnection()) {
            connection.setAutoCommit(false);
            try {
                MetodoPagoVentaInterna metodo = command.metodoPago() == null ? MetodoPagoVentaInterna.EFECTIVO : command.metodoPago();
                BigDecimal total = calcularTotal(command.detalles());
                long ventaId = insertarVenta(connection, command, metodo, total);
                for (DetalleVentaInternaAvanzada detalle : command.detalles()) {
                    BigDecimal stockAnterior = leerStockActual(connection, detalle.productoId());
                    BigDecimal stockNuevo = stockAnterior.subtract(detalle.cantidad());
                    if (stockNuevo.compareTo(BigDecimal.ZERO) < 0) throw new ValidationException("No hay stock suficiente para registrar esta salida.");
                    BigDecimal subtotal = detalle.cantidad().multiply(detalle.precioUnitario());
                    insertarDetalle(connection, ventaId, detalle, subtotal);
                    actualizarStock(connection, detalle.productoId(), stockNuevo);
                    insertarMovimientoSalida(connection, detalle.productoId(), detalle.cantidad(), stockAnterior, stockNuevo, ventaId);
                }
                if (metodo == MetodoPagoVentaInterna.FIADO) {
                    insertarCuentaPorCobrar(connection, command.clienteFiadoId(), ventaId, total, command.observacion());
                } else {
                    insertarPagoVenta(connection, ventaId, total, metodo, command.numeroReferencia(), command.observacion());
                }
                connection.commit();
                return findById(ventaId);
            } catch (RuntimeException | SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (ValidationException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo registrar la venta interna avanzada.", ex);
        }
    }

    @Override
    public AnulacionVentaInterna anularVenta(RegistroAnulacionVentaInterna command) {
        try (Connection connection = connectionFactory.openConnection()) {
            connection.setAutoCommit(false);
            try {
                VentaInterna venta = leerVentaParaAnular(connection, command.ventaInternaId());
                if ("ANULADA".equalsIgnoreCase(venta.estado())) throw new ValidationException("La venta interna ya está anulada.");
                validarCarteraSinAbonos(connection, command.ventaInternaId());
                List<DetalleRow> detalles = leerDetalles(connection, command.ventaInternaId());
                for (DetalleRow detalle : detalles) {
                    BigDecimal stockAnterior = leerStockActualIncluyeInactivos(connection, detalle.productoId());
                    BigDecimal stockNuevo = stockAnterior.add(detalle.cantidad());
                    actualizarStock(connection, detalle.productoId(), stockNuevo);
                    insertarMovimientoAnulacion(connection, detalle.productoId(), detalle.cantidad(), stockAnterior, stockNuevo, command.ventaInternaId(), command.motivo());
                }
                anularCuentaPorCobrarSiExiste(connection, command.ventaInternaId());
                marcarVentaAnulada(connection, command.ventaInternaId());
                long anulacionId = insertarAnulacion(connection, command);
                connection.commit();
                return findAnulacionById(anulacionId);
            } catch (RuntimeException | SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (ValidationException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo anular la venta interna.", ex);
        }
    }

    private BigDecimal calcularTotal(List<DetalleVentaInternaAvanzada> detalles) {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleVentaInternaAvanzada detalle : detalles) {
            total = total.add(detalle.cantidad().multiply(detalle.precioUnitario()));
        }
        return total;
    }

    private BigDecimal leerStockActual(Connection connection, Long productoId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT stock_actual FROM producto WHERE id = ? AND estado = 'ACTIVO'")) {
            statement.setLong(1, productoId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) throw new ValidationException("El producto seleccionado no existe o está inactivo.");
                BigDecimal stock = rs.getBigDecimal("stock_actual");
                return stock == null ? BigDecimal.ZERO : stock;
            }
        }
    }

    private BigDecimal leerStockActualIncluyeInactivos(Connection connection, Long productoId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT stock_actual FROM producto WHERE id = ?")) {
            statement.setLong(1, productoId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) throw new ValidationException("El producto de la venta ya no existe.");
                BigDecimal stock = rs.getBigDecimal("stock_actual");
                return stock == null ? BigDecimal.ZERO : stock;
            }
        }
    }

    private long insertarVenta(Connection connection, RegistroVentaInternaAvanzada command, MetodoPagoVentaInterna metodo, BigDecimal total) throws SQLException {
        String sql = """
                INSERT INTO venta_interna (cliente_fiado_id, total, metodo_pago, numero_referencia,
                    advertencia_tributaria_aceptada, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setNullableLong(statement, 1, command.clienteFiadoId());
            statement.setBigDecimal(2, total);
            statement.setString(3, metodo.dbValue());
            statement.setString(4, blankToNull(command.numeroReferencia()));
            statement.setInt(5, command.advertenciaTributariaAceptada() ? 1 : 0);
            statement.setString(6, blankToNull(command.observacion()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1); }
        }
        throw new InfrastructureException("No se pudo obtener el ID de la venta creada.");
    }

    private void insertarDetalle(Connection connection, long ventaId, DetalleVentaInternaAvanzada detalle, BigDecimal subtotal) throws SQLException {
        String sql = """
                INSERT INTO detalle_venta_interna (venta_interna_id, producto_id, cantidad, precio_unitario, subtotal, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, ventaId);
            statement.setLong(2, detalle.productoId());
            statement.setBigDecimal(3, detalle.cantidad());
            statement.setBigDecimal(4, detalle.precioUnitario());
            statement.setBigDecimal(5, subtotal);
            statement.setString(6, blankToNull(detalle.observacion()));
            statement.executeUpdate();
        }
    }

    private void insertarPagoVenta(Connection connection, long ventaId, BigDecimal monto, MetodoPagoVentaInterna metodo, String referencia, String observacion) throws SQLException {
        String sql = """
                INSERT INTO venta_pago (venta_interna_id, monto, metodo_pago, referencia, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, ventaId);
            statement.setBigDecimal(2, monto);
            statement.setString(3, metodo.dbValue());
            statement.setString(4, blankToNull(referencia));
            statement.setString(5, blankToNull(observacion));
            statement.executeUpdate();
        }
    }

    private void insertarCuentaPorCobrar(Connection connection, Long clienteFiadoId, long ventaId, BigDecimal total, String observacion) throws SQLException {
        String sql = """
                INSERT INTO cuenta_por_cobrar (cliente_fiado_id, venta_interna_id, monto_original, saldo_pendiente, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, clienteFiadoId);
            statement.setLong(2, ventaId);
            statement.setBigDecimal(3, total);
            statement.setBigDecimal(4, total);
            statement.setString(5, blankToNull(observacion));
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

    private void insertarMovimientoSalida(Connection connection, Long productoId, BigDecimal cantidad, BigDecimal stockAnterior, BigDecimal stockNuevo, long ventaId) throws SQLException {
        insertarMovimiento(connection, productoId, "SALIDA_VENTA_INTERNA", cantidad, stockAnterior, stockNuevo, ventaId, "Salida por venta interna no tributaria");
    }

    private void insertarMovimientoAnulacion(Connection connection, Long productoId, BigDecimal cantidad, BigDecimal stockAnterior, BigDecimal stockNuevo, long ventaId, String motivo) throws SQLException {
        insertarMovimiento(connection, productoId, "CORRECCION", cantidad, stockAnterior, stockNuevo, ventaId, "Anulación de venta interna: " + normalize(motivo));
    }

    private void insertarMovimiento(Connection connection, Long productoId, String tipo, BigDecimal cantidad, BigDecimal stockAnterior, BigDecimal stockNuevo, long ventaId, String motivo) throws SQLException {
        String sql = """
                INSERT INTO movimiento_inventario (producto_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo,
                    referencia_tipo, referencia_id, motivo, updated_at)
                VALUES (?, ?, ?, ?, ?, 'VENTA_INTERNA', ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productoId);
            statement.setString(2, tipo);
            statement.setBigDecimal(3, cantidad);
            statement.setBigDecimal(4, stockAnterior);
            statement.setBigDecimal(5, stockNuevo);
            statement.setLong(6, ventaId);
            statement.setString(7, motivo);
            statement.executeUpdate();
        }
    }

    private VentaInterna leerVentaParaAnular(Connection connection, Long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id, fecha_venta, total, metodo_pago, numero_referencia, estado, advertencia_tributaria_aceptada, observacion FROM venta_interna WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        throw new ValidationException("La venta interna seleccionada no existe.");
    }

    private void validarCarteraSinAbonos(Connection connection, Long ventaId) throws SQLException {
        String sql = """
                SELECT COUNT(a.id) AS total_abonos
                FROM cuenta_por_cobrar c
                LEFT JOIN abono a ON a.cuenta_por_cobrar_id = c.id
                WHERE c.venta_interna_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, ventaId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next() && rs.getInt("total_abonos") > 0) {
                    throw new ValidationException("No se puede anular una venta fiada que ya tiene abonos registrados.");
                }
            }
        }
    }

    private List<DetalleRow> leerDetalles(Connection connection, Long ventaId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT producto_id, cantidad FROM detalle_venta_interna WHERE venta_interna_id = ?")) {
            statement.setLong(1, ventaId);
            try (ResultSet rs = statement.executeQuery()) {
                List<DetalleRow> detalles = new ArrayList<>();
                while (rs.next()) detalles.add(new DetalleRow(rs.getLong("producto_id"), rs.getBigDecimal("cantidad")));
                if (detalles.isEmpty()) throw new ValidationException("La venta no tiene detalles para reversar.");
                return detalles;
            }
        }
    }

    private void anularCuentaPorCobrarSiExiste(Connection connection, Long ventaId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE cuenta_por_cobrar SET estado = 'ANULADA', saldo_pendiente = 0, updated_at = datetime('now') WHERE venta_interna_id = ?")) {
            statement.setLong(1, ventaId);
            statement.executeUpdate();
        }
    }

    private void marcarVentaAnulada(Connection connection, Long ventaId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE venta_interna SET estado = 'ANULADA', updated_at = datetime('now') WHERE id = ?")) {
            statement.setLong(1, ventaId);
            statement.executeUpdate();
        }
    }

    private long insertarAnulacion(Connection connection, RegistroAnulacionVentaInterna command) throws SQLException {
        String sql = """
                INSERT INTO anulacion_venta (venta_interna_id, motivo, responsable_texto, updated_at)
                VALUES (?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, command.ventaInternaId());
            statement.setString(2, normalize(command.motivo()));
            statement.setString(3, blankToNull(command.responsableTexto()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1); }
        }
        throw new InfrastructureException("No se pudo obtener el ID de la anulación creada.");
    }

    private VentaInterna findById(long id) throws SQLException {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, fecha_venta, total, metodo_pago, numero_referencia, estado, advertencia_tributaria_aceptada, observacion FROM venta_interna WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) { if (rs.next()) return map(rs); }
        }
        throw new InfrastructureException("No se pudo leer la venta interna creada.");
    }

    private AnulacionVentaInterna findAnulacionById(long id) throws SQLException {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, venta_interna_id, fecha_anulacion, motivo, responsable_texto FROM anulacion_venta WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String fecha = rs.getString("fecha_anulacion");
                    return new AnulacionVentaInterna(rs.getLong("id"), rs.getLong("venta_interna_id"),
                            fecha == null ? null : LocalDateTime.parse(fecha.replace(' ', 'T')),
                            rs.getString("motivo"), rs.getString("responsable_texto"));
                }
            }
        }
        throw new InfrastructureException("No se pudo leer la anulación creada.");
    }

    private VentaInterna map(ResultSet rs) throws SQLException {
        String fecha = rs.getString("fecha_venta");
        return new VentaInterna(rs.getLong("id"), fecha == null ? null : LocalDateTime.parse(fecha.replace(' ', 'T')),
                rs.getBigDecimal("total"), MetodoPagoVentaInterna.fromDb(rs.getString("metodo_pago")),
                rs.getString("numero_referencia"), rs.getString("estado"), rs.getInt("advertencia_tributaria_aceptada") == 1,
                rs.getString("observacion"));
    }

    private record DetalleRow(Long productoId, BigDecimal cantidad) {}
}
