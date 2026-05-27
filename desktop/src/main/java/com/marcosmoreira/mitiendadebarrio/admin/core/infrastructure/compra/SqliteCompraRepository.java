package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.compra;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.compra.CompraRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.Compra;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.CuentaPorPagar;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.DetalleCompraAvanzada;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.RegistroCompraAvanzada;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.RegistroCompraSimple;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.RegistroPagoProveedor;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.TipoComprobanteCompra;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Adaptador SQLite para compras, entradas de mercadería y cuentas por pagar. */
public final class SqliteCompraRepository extends SqliteRepositorySupport implements CompraRepository {

    public SqliteCompraRepository(SqliteConnectionFactory connectionFactory) { super(connectionFactory); }

    @Override
    public List<Compra> findRecent(String query, int limit) {
        String sql = """
                SELECT c.id, c.proveedor_id, p.nombre AS proveedor_nombre, c.fecha_compra, c.numero_comprobante,
                       c.tipo_comprobante, c.total_estimado, c.estado, c.observacion
                FROM compra c
                LEFT JOIN proveedor p ON p.id = c.proveedor_id
                WHERE (? IS NULL OR p.nombre LIKE ? OR c.numero_comprobante LIKE ? OR c.observacion LIKE ?)
                ORDER BY c.fecha_compra DESC, c.id DESC
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
                List<Compra> items = new ArrayList<>();
                while (rs.next()) items.add(map(rs));
                return items;
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudieron listar las compras.", ex);
        }
    }

    @Override
    public Compra registrarCompraSimple(RegistroCompraSimple command) {
        try (Connection connection = connectionFactory.openConnection()) {
            connection.setAutoCommit(false);
            try {
                ProductoStockData producto = leerProducto(connection, command.productoId());
                BigDecimal subtotal = command.cantidad().multiply(command.costoUnitario());
                long compraId = insertarCompra(connection, command.proveedorId(), command.fechaCompra(), command.tipoComprobante(), command.numeroComprobante(), subtotal, command.observacion());
                Long loteId = insertarLoteSiAplica(connection, command.proveedorId(), command.productoId(), command.codigoLote(), command.fechaCompra(), command.fechaVencimiento(), command.cantidad(), command.costoUnitario(), command.observacion(), producto);
                insertarDetalle(connection, compraId, command.productoId(), loteId, command.cantidad(), command.costoUnitario(), subtotal, command.codigoLote(), command.fechaVencimiento(), command.observacion());
                BigDecimal stockNuevo = producto.stockActual.add(command.cantidad());
                actualizarStock(connection, command.productoId(), stockNuevo, command.costoUnitario());
                insertarMovimiento(connection, command.productoId(), loteId, command.cantidad(), producto.stockActual, stockNuevo, compraId, "Entrada por compra/recepción de mercadería");
                connection.commit();
                return findById(compraId);
            } catch (RuntimeException | SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (ValidationException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo registrar la compra.", ex);
        }
    }

    @Override
    public Compra registrarCompraAvanzada(RegistroCompraAvanzada command) {
        try (Connection connection = connectionFactory.openConnection()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal total = command.detalles().stream()
                        .map(d -> d.cantidad().multiply(d.costoUnitario()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                long compraId = insertarCompra(connection, command.proveedorId(), command.fechaCompra(), command.tipoComprobante(), command.numeroComprobante(), total, command.observacion());
                for (DetalleCompraAvanzada detalle : command.detalles()) {
                    ProductoStockData producto = leerProducto(connection, detalle.productoId());
                    BigDecimal subtotal = detalle.cantidad().multiply(detalle.costoUnitario());
                    Long loteId = insertarLoteSiAplica(connection, command.proveedorId(), detalle.productoId(), detalle.codigoLote(), command.fechaCompra(), detalle.fechaVencimiento(), detalle.cantidad(), detalle.costoUnitario(), detalle.observacion(), producto);
                    insertarDetalle(connection, compraId, detalle.productoId(), loteId, detalle.cantidad(), detalle.costoUnitario(), subtotal, detalle.codigoLote(), detalle.fechaVencimiento(), detalle.observacion());
                    BigDecimal stockNuevo = producto.stockActual.add(detalle.cantidad());
                    actualizarStock(connection, detalle.productoId(), stockNuevo, detalle.costoUnitario());
                    insertarMovimiento(connection, detalle.productoId(), loteId, detalle.cantidad(), producto.stockActual, stockNuevo, compraId, "Entrada por compra avanzada");
                }
                if (command.compraCredito()) {
                    insertarCuentaPorPagar(connection, compraId, command.proveedorId(), command.fechaCompra(), command.fechaVencimientoPago(), total, command.observacion());
                }
                connection.commit();
                return findById(compraId);
            } catch (RuntimeException | SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (ValidationException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo registrar la compra avanzada.", ex);
        }
    }

    @Override
    public CuentaPorPagar registrarPagoProveedor(RegistroPagoProveedor command) {
        try (Connection connection = connectionFactory.openConnection()) {
            connection.setAutoCommit(false);
            try {
                CuentaPorPagar cuenta = leerCuentaPorPagar(connection, command.cuentaPorPagarId());
                if (!"PENDIENTE".equals(cuenta.estado()) && !"PARCIAL".equals(cuenta.estado())) {
                    throw new ValidationException("La cuenta por pagar no está pendiente de pago.");
                }
                if (command.monto().compareTo(cuenta.saldoPendiente()) > 0) {
                    throw new ValidationException("El pago no puede superar el saldo pendiente.");
                }
                insertarPagoProveedor(connection, command);
                BigDecimal saldoNuevo = cuenta.saldoPendiente().subtract(command.monto());
                String estadoNuevo = saldoNuevo.compareTo(BigDecimal.ZERO) == 0 ? "PAGADA" : "PARCIAL";
                actualizarCuentaPorPagar(connection, command.cuentaPorPagarId(), saldoNuevo, estadoNuevo);
                connection.commit();
                return leerCuentaPorPagar(command.cuentaPorPagarId());
            } catch (RuntimeException | SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (ValidationException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo registrar el pago a proveedor.", ex);
        }
    }

    @Override
    public List<CuentaPorPagar> listarCuentasPorPagarPendientes(int limit) {
        String sql = """
                SELECT cpp.id, cpp.compra_id, cpp.proveedor_id, p.nombre AS proveedor_nombre,
                       cpp.fecha_emision, cpp.fecha_vencimiento, cpp.monto_total, cpp.saldo_pendiente,
                       cpp.estado, cpp.observacion
                FROM cuenta_por_pagar cpp
                LEFT JOIN proveedor p ON p.id = cpp.proveedor_id
                WHERE cpp.estado IN ('PENDIENTE','PARCIAL')
                ORDER BY cpp.fecha_vencimiento IS NULL, cpp.fecha_vencimiento, cpp.id DESC
                LIMIT ?
                """;
        try (Connection connection = connectionFactory.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Math.max(1, limit));
            try (ResultSet rs = statement.executeQuery()) {
                List<CuentaPorPagar> items = new ArrayList<>();
                while (rs.next()) items.add(mapCuentaPorPagar(rs));
                return items;
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudieron listar las cuentas por pagar.", ex);
        }
    }

    private ProductoStockData leerProducto(Connection connection, Long productoId) throws SQLException {
        String sql = "SELECT stock_actual, maneja_lote, maneja_vencimiento FROM producto WHERE id = ? AND estado = 'ACTIVO'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productoId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) throw new ValidationException("El producto seleccionado no existe o está inactivo.");
                BigDecimal stock = rs.getBigDecimal("stock_actual");
                return new ProductoStockData(stock == null ? BigDecimal.ZERO : stock, rs.getInt("maneja_lote") == 1, rs.getInt("maneja_vencimiento") == 1);
            }
        }
    }

    private long insertarCompra(Connection connection, Long proveedorId, LocalDate fechaCompra, TipoComprobanteCompra tipoComprobante, String numeroComprobante, BigDecimal total, String observacion) throws SQLException {
        String sql = """
                INSERT INTO compra (proveedor_id, fecha_compra, numero_comprobante, tipo_comprobante, total_estimado, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setNullableLong(statement, 1, proveedorId);
            statement.setString(2, fechaCompra == null ? LocalDate.now().toString() : fechaCompra.toString());
            statement.setString(3, blankToNull(numeroComprobante));
            statement.setString(4, tipoComprobante == null ? TipoComprobanteCompra.SIN_COMPROBANTE.dbValue() : tipoComprobante.dbValue());
            statement.setBigDecimal(5, total);
            statement.setString(6, blankToNull(observacion));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1); }
        }
        throw new InfrastructureException("No se pudo obtener el ID de la compra creada.");
    }

    private Long insertarLoteSiAplica(Connection connection, Long proveedorId, Long productoId, String codigoLote, LocalDate fechaCompra, LocalDate fechaVencimiento, BigDecimal cantidad, BigDecimal costoUnitario, String observacion, ProductoStockData producto) throws SQLException {
        if (!producto.manejaLote && !producto.manejaVencimiento && (codigoLote == null || codigoLote.isBlank()) && fechaVencimiento == null) {
            return null;
        }
        String sql = """
                INSERT INTO lote_producto (producto_id, proveedor_id, codigo_lote, fecha_recepcion, fecha_vencimiento,
                    cantidad_inicial, cantidad_actual, costo_unitario, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, productoId);
            setNullableLong(statement, 2, proveedorId);
            statement.setString(3, blankToNull(codigoLote));
            statement.setString(4, fechaCompra == null ? LocalDate.now().toString() : fechaCompra.toString());
            statement.setString(5, fechaVencimiento == null ? null : fechaVencimiento.toString());
            statement.setBigDecimal(6, cantidad);
            statement.setBigDecimal(7, cantidad);
            statement.setBigDecimal(8, costoUnitario);
            statement.setString(9, blankToNull(observacion));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1); }
        }
        throw new InfrastructureException("No se pudo obtener el ID del lote creado.");
    }

    private void insertarDetalle(Connection connection, long compraId, Long productoId, Long loteId, BigDecimal cantidad, BigDecimal costoUnitario, BigDecimal subtotal, String codigoLote, LocalDate fechaVencimiento, String observacion) throws SQLException {
        String sql = """
                INSERT INTO detalle_compra (compra_id, producto_id, lote_id, cantidad, costo_unitario, subtotal, codigo_lote, fecha_vencimiento, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, compraId);
            statement.setLong(2, productoId);
            setNullableLong(statement, 3, loteId);
            statement.setBigDecimal(4, cantidad);
            statement.setBigDecimal(5, costoUnitario);
            statement.setBigDecimal(6, subtotal);
            statement.setString(7, blankToNull(codigoLote));
            statement.setString(8, fechaVencimiento == null ? null : fechaVencimiento.toString());
            statement.setString(9, blankToNull(observacion));
            statement.executeUpdate();
        }
    }

    private void actualizarStock(Connection connection, Long productoId, BigDecimal stockNuevo, BigDecimal costoUnitario) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE producto SET stock_actual = ?, precio_compra_referencia = ?, updated_at = datetime('now') WHERE id = ?")) {
            statement.setBigDecimal(1, stockNuevo);
            statement.setBigDecimal(2, costoUnitario);
            statement.setLong(3, productoId);
            statement.executeUpdate();
        }
    }

    private void insertarMovimiento(Connection connection, Long productoId, Long loteId, BigDecimal cantidad, BigDecimal stockAnterior, BigDecimal stockNuevo, long compraId, String motivo) throws SQLException {
        String sql = """
                INSERT INTO movimiento_inventario (producto_id, lote_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo,
                    referencia_tipo, referencia_id, motivo, updated_at)
                VALUES (?, ?, 'ENTRADA_COMPRA', ?, ?, ?, 'COMPRA', ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productoId);
            setNullableLong(statement, 2, loteId);
            statement.setBigDecimal(3, cantidad);
            statement.setBigDecimal(4, stockAnterior);
            statement.setBigDecimal(5, stockNuevo);
            statement.setLong(6, compraId);
            statement.setString(7, motivo);
            statement.executeUpdate();
        }
    }

    private long insertarCuentaPorPagar(Connection connection, long compraId, Long proveedorId, LocalDate fechaCompra, LocalDate fechaVencimiento, BigDecimal total, String observacion) throws SQLException {
        String sql = """
                INSERT INTO cuenta_por_pagar (compra_id, proveedor_id, fecha_emision, fecha_vencimiento, monto_total, saldo_pendiente, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, compraId);
            setNullableLong(statement, 2, proveedorId);
            statement.setString(3, fechaCompra == null ? LocalDate.now().toString() : fechaCompra.toString());
            statement.setString(4, fechaVencimiento == null ? null : fechaVencimiento.toString());
            statement.setBigDecimal(5, total);
            statement.setBigDecimal(6, total);
            statement.setString(7, blankToNull(observacion));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1); }
        }
        throw new InfrastructureException("No se pudo crear la cuenta por pagar.");
    }

    private void insertarPagoProveedor(Connection connection, RegistroPagoProveedor command) throws SQLException {
        String sql = """
                INSERT INTO pago_proveedor (cuenta_por_pagar_id, fecha_pago, monto, forma_pago, referencia, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, command.cuentaPorPagarId());
            statement.setString(2, command.fechaPago() == null ? LocalDate.now().toString() : command.fechaPago().toString());
            statement.setBigDecimal(3, command.monto());
            statement.setString(4, command.formaPago() == null || command.formaPago().isBlank() ? "EFECTIVO" : command.formaPago().strip().toUpperCase());
            statement.setString(5, blankToNull(command.referencia()));
            statement.setString(6, blankToNull(command.observacion()));
            statement.executeUpdate();
        }
    }

    private void actualizarCuentaPorPagar(Connection connection, Long cuentaId, BigDecimal saldoNuevo, String estadoNuevo) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE cuenta_por_pagar SET saldo_pendiente = ?, estado = ?, updated_at = datetime('now') WHERE id = ?")) {
            statement.setBigDecimal(1, saldoNuevo);
            statement.setString(2, estadoNuevo);
            statement.setLong(3, cuentaId);
            statement.executeUpdate();
        }
    }

    private CuentaPorPagar leerCuentaPorPagar(Long cuentaId) throws SQLException {
        try (Connection connection = connectionFactory.openConnection()) {
            return leerCuentaPorPagar(connection, cuentaId);
        }
    }

    private CuentaPorPagar leerCuentaPorPagar(Connection connection, Long cuentaId) throws SQLException {
        String sql = """
                SELECT cpp.id, cpp.compra_id, cpp.proveedor_id, p.nombre AS proveedor_nombre,
                       cpp.fecha_emision, cpp.fecha_vencimiento, cpp.monto_total, cpp.saldo_pendiente,
                       cpp.estado, cpp.observacion
                FROM cuenta_por_pagar cpp
                LEFT JOIN proveedor p ON p.id = cpp.proveedor_id
                WHERE cpp.id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cuentaId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return mapCuentaPorPagar(rs);
            }
        }
        throw new ValidationException("La cuenta por pagar seleccionada no existe.");
    }

    private Compra findById(long id) throws SQLException {
        String sql = """
                SELECT c.id, c.proveedor_id, p.nombre AS proveedor_nombre, c.fecha_compra, c.numero_comprobante,
                       c.tipo_comprobante, c.total_estimado, c.estado, c.observacion
                FROM compra c LEFT JOIN proveedor p ON p.id = c.proveedor_id WHERE c.id = ?
                """;
        try (Connection connection = connectionFactory.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) { if (rs.next()) return map(rs); }
        }
        throw new InfrastructureException("No se pudo leer la compra creada.");
    }

    private Compra map(ResultSet rs) throws SQLException {
        String fecha = rs.getString("fecha_compra");
        return new Compra(rs.getLong("id"), nullableLong(rs, "proveedor_id"), rs.getString("proveedor_nombre"),
                fecha == null ? null : LocalDate.parse(fecha), rs.getString("numero_comprobante"),
                TipoComprobanteCompra.fromDb(rs.getString("tipo_comprobante")), rs.getBigDecimal("total_estimado"),
                rs.getString("estado"), rs.getString("observacion"));
    }

    private CuentaPorPagar mapCuentaPorPagar(ResultSet rs) throws SQLException {
        String emision = rs.getString("fecha_emision");
        String vencimiento = rs.getString("fecha_vencimiento");
        return new CuentaPorPagar(
                rs.getLong("id"),
                rs.getLong("compra_id"),
                nullableLong(rs, "proveedor_id"),
                rs.getString("proveedor_nombre"),
                emision == null ? null : LocalDate.parse(emision),
                vencimiento == null ? null : LocalDate.parse(vencimiento),
                rs.getBigDecimal("monto_total"),
                rs.getBigDecimal("saldo_pendiente"),
                rs.getString("estado"),
                rs.getString("observacion")
        );
    }

    private record ProductoStockData(BigDecimal stockActual, boolean manejaLote, boolean manejaVencimiento) {}
}
