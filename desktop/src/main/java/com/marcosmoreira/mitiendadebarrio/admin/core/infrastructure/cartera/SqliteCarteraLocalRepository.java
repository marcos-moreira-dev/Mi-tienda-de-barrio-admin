package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.cartera;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.cartera.CarteraLocalRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja.MetodoPagoCaja;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja.TipoMovimientoCaja;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.cartera.CarteraCajaResultado;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.cartera.RegistroAbonoConCaja;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.cartera.RegistroPagoProveedorConCaja;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.cartera.RegistroVentaPagadaEnCaja;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;

import java.math.BigDecimal;
import java.sql.*;

/** Adaptador SQLite para operaciones integradas de cartera y caja local. */
public final class SqliteCarteraLocalRepository extends SqliteRepositorySupport implements CarteraLocalRepository {

    public SqliteCarteraLocalRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public CarteraCajaResultado registrarAbonoConCaja(RegistroAbonoConCaja command) {
        try (Connection connection = connectionFactory.openConnection()) {
            connection.setAutoCommit(false);
            try {
                validarCajaAbierta(connection, command.cajaDiariaId());
                CuentaCobrarRow cuenta = leerCuentaPorCobrar(connection, command.cuentaPorCobrarId());
                if (!"ABIERTA".equals(cuenta.estado())) throw new ValidationException("Solo se puede abonar a cuentas por cobrar abiertas.");
                if (command.monto().compareTo(cuenta.saldoPendiente()) > 0) throw new ValidationException("El abono no puede ser mayor que el saldo pendiente.");

                long movimientoId = insertarMovimientoCaja(connection, command.cajaDiariaId(), TipoMovimientoCaja.INGRESO,
                        "ABONO_FIADO", null, command.monto(), metodo(command.metodoPago()),
                        "Abono de fiado sobre cuenta #" + command.cuentaPorCobrarId());
                long abonoId = insertarAbono(connection, command.cuentaPorCobrarId(), movimientoId, command.monto(), metodo(command.metodoPago()), command.observacion());
                actualizarReferenciaMovimiento(connection, movimientoId, abonoId);

                BigDecimal nuevoSaldo = cuenta.saldoPendiente().subtract(command.monto());
                actualizarCuentaPorCobrar(connection, command.cuentaPorCobrarId(), nuevoSaldo);
                recalcularCaja(connection, command.cajaDiariaId());
                connection.commit();
                return new CarteraCajaResultado("ABONO_FIADO", abonoId, movimientoId, command.monto(), nuevoSaldo);
            } catch (RuntimeException | SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (ValidationException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo registrar el abono conectado a caja.", ex);
        }
    }

    @Override
    public CarteraCajaResultado registrarPagoProveedorConCaja(RegistroPagoProveedorConCaja command) {
        try (Connection connection = connectionFactory.openConnection()) {
            connection.setAutoCommit(false);
            try {
                validarCajaAbierta(connection, command.cajaDiariaId());
                CuentaPagarRow cuenta = leerCuentaPorPagar(connection, command.cuentaPorPagarId());
                if (!"PENDIENTE".equals(cuenta.estado()) && !"PARCIAL".equals(cuenta.estado())) {
                    throw new ValidationException("Solo se puede pagar una cuenta por pagar pendiente o parcial.");
                }
                if (command.monto().compareTo(cuenta.saldoPendiente()) > 0) throw new ValidationException("El pago no puede ser mayor que el saldo pendiente.");

                long movimientoId = insertarMovimientoCaja(connection, command.cajaDiariaId(), TipoMovimientoCaja.EGRESO,
                        "PAGO_PROVEEDOR", null, command.monto(), metodo(command.metodoPago()),
                        "Pago a proveedor sobre cuenta #" + command.cuentaPorPagarId());
                long pagoId = insertarPagoProveedor(connection, command.cuentaPorPagarId(), movimientoId, command.monto(), metodo(command.metodoPago()), command.referencia(), command.observacion());
                actualizarReferenciaMovimiento(connection, movimientoId, pagoId);

                BigDecimal nuevoSaldo = cuenta.saldoPendiente().subtract(command.monto());
                actualizarCuentaPorPagar(connection, command.cuentaPorPagarId(), nuevoSaldo);
                recalcularCaja(connection, command.cajaDiariaId());
                connection.commit();
                return new CarteraCajaResultado("PAGO_PROVEEDOR", pagoId, movimientoId, command.monto(), nuevoSaldo);
            } catch (RuntimeException | SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (ValidationException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo registrar el pago a proveedor conectado a caja.", ex);
        }
    }

    @Override
    public CarteraCajaResultado registrarVentaPagadaEnCaja(RegistroVentaPagadaEnCaja command) {
        try (Connection connection = connectionFactory.openConnection()) {
            connection.setAutoCommit(false);
            try {
                validarCajaAbierta(connection, command.cajaDiariaId());
                VentaPagoRow pago = leerPagoVentaPendienteCaja(connection, command.ventaInternaId());
                long movimientoId = insertarMovimientoCaja(connection, command.cajaDiariaId(), TipoMovimientoCaja.INGRESO,
                        "VENTA_INTERNA", command.ventaInternaId(), pago.monto(), MetodoPagoCaja.fromDb(pago.metodoPago()),
                        command.observacion() == null || command.observacion().isBlank()
                                ? "Ingreso por venta interna #" + command.ventaInternaId()
                                : command.observacion().strip());
                vincularPagoVenta(connection, pago.id(), movimientoId);
                recalcularCaja(connection, command.cajaDiariaId());
                connection.commit();
                return new CarteraCajaResultado("VENTA_INTERNA", pago.id(), movimientoId, pago.monto(), BigDecimal.ZERO);
            } catch (RuntimeException | SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (ValidationException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo conectar la venta pagada con caja.", ex);
        }
    }

    private void validarCajaAbierta(Connection connection, Long cajaDiariaId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT estado FROM caja_diaria WHERE id = ?")) {
            statement.setLong(1, cajaDiariaId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) throw new ValidationException("La caja diaria seleccionada no existe.");
                if (!"ABIERTA".equals(rs.getString("estado"))) throw new ValidationException("Solo se puede mover dinero en una caja abierta.");
            }
        }
    }

    private CuentaCobrarRow leerCuentaPorCobrar(Connection connection, Long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id, saldo_pendiente, estado FROM cuenta_por_cobrar WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return new CuentaCobrarRow(rs.getLong("id"), nvl(rs.getBigDecimal("saldo_pendiente")), rs.getString("estado"));
            }
        }
        throw new ValidationException("La cuenta por cobrar seleccionada no existe.");
    }

    private CuentaPagarRow leerCuentaPorPagar(Connection connection, Long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id, saldo_pendiente, estado FROM cuenta_por_pagar WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return new CuentaPagarRow(rs.getLong("id"), nvl(rs.getBigDecimal("saldo_pendiente")), rs.getString("estado"));
            }
        }
        throw new ValidationException("La cuenta por pagar seleccionada no existe.");
    }

    private VentaPagoRow leerPagoVentaPendienteCaja(Connection connection, Long ventaId) throws SQLException {
        String sql = """
                SELECT vp.id, vp.monto, vp.metodo_pago, vi.estado, vi.metodo_pago AS metodo_venta
                FROM venta_pago vp
                JOIN venta_interna vi ON vi.id = vp.venta_interna_id
                WHERE vp.venta_interna_id = ? AND vp.movimiento_caja_id IS NULL
                ORDER BY vp.id
                LIMIT 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, ventaId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    if (!"REGISTRADA".equals(rs.getString("estado"))) throw new ValidationException("Solo se conectan a caja ventas registradas.");
                    if ("FIADO".equals(rs.getString("metodo_venta"))) throw new ValidationException("Una venta fiada no entra a caja hasta registrar un abono.");
                    return new VentaPagoRow(rs.getLong("id"), nvl(rs.getBigDecimal("monto")), rs.getString("metodo_pago"));
                }
            }
        }
        throw new ValidationException("La venta no tiene un pago pendiente de conectar con caja.");
    }

    private long insertarMovimientoCaja(Connection connection, Long cajaId, TipoMovimientoCaja tipo, String origen, Long referenciaId, BigDecimal monto, MetodoPagoCaja metodo, String descripcion) throws SQLException {
        String sql = """
                INSERT INTO movimiento_caja(caja_diaria_id,tipo_movimiento,origen,referencia_id,monto,metodo_pago,descripcion,updated_at)
                VALUES(?,?,?,?,?,?,?,datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, cajaId);
            statement.setString(2, tipo.dbValue());
            statement.setString(3, origen);
            if (referenciaId == null) statement.setNull(4, Types.INTEGER); else statement.setLong(4, referenciaId);
            statement.setBigDecimal(5, monto);
            statement.setString(6, metodo.dbValue());
            statement.setString(7, descripcion == null || descripcion.isBlank() ? origen : descripcion.strip());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        throw new InfrastructureException("No se pudo obtener el ID del movimiento de caja.");
    }

    private long insertarAbono(Connection connection, Long cuentaId, Long movimientoId, BigDecimal monto, MetodoPagoCaja metodo, String observacion) throws SQLException {
        String sql = """
                INSERT INTO abono (cuenta_por_cobrar_id, movimiento_caja_id, monto, metodo_pago, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, cuentaId);
            statement.setLong(2, movimientoId);
            statement.setBigDecimal(3, monto);
            statement.setString(4, metodo.dbValue());
            statement.setString(5, blank(observacion));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1); }
        }
        throw new InfrastructureException("No se pudo obtener el ID del abono.");
    }

    private long insertarPagoProveedor(Connection connection, Long cuentaId, Long movimientoId, BigDecimal monto, MetodoPagoCaja metodo, String referencia, String observacion) throws SQLException {
        String sql = """
                INSERT INTO pago_proveedor (cuenta_por_pagar_id, movimiento_caja_id, monto, forma_pago, referencia, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, cuentaId);
            statement.setLong(2, movimientoId);
            statement.setBigDecimal(3, monto);
            statement.setString(4, metodo.dbValue());
            statement.setString(5, blank(referencia));
            statement.setString(6, blank(observacion));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1); }
        }
        throw new InfrastructureException("No se pudo obtener el ID del pago a proveedor.");
    }

    private void actualizarCuentaPorCobrar(Connection connection, Long cuentaId, BigDecimal nuevoSaldo) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE cuenta_por_cobrar SET saldo_pendiente=?, estado=?, updated_at=datetime('now') WHERE id=?")) {
            statement.setBigDecimal(1, nuevoSaldo);
            statement.setString(2, nuevoSaldo.compareTo(BigDecimal.ZERO) == 0 ? "CERRADA" : "ABIERTA");
            statement.setLong(3, cuentaId);
            statement.executeUpdate();
        }
    }

    private void actualizarCuentaPorPagar(Connection connection, Long cuentaId, BigDecimal nuevoSaldo) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE cuenta_por_pagar SET saldo_pendiente=?, estado=?, updated_at=datetime('now') WHERE id=?")) {
            statement.setBigDecimal(1, nuevoSaldo);
            statement.setString(2, nuevoSaldo.compareTo(BigDecimal.ZERO) == 0 ? "PAGADA" : "PARCIAL");
            statement.setLong(3, cuentaId);
            statement.executeUpdate();
        }
    }

    private void actualizarReferenciaMovimiento(Connection connection, Long movimientoId, Long referenciaId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE movimiento_caja SET referencia_id=?, updated_at=datetime('now') WHERE id=?")) {
            statement.setLong(1, referenciaId);
            statement.setLong(2, movimientoId);
            statement.executeUpdate();
        }
    }

    private void vincularPagoVenta(Connection connection, Long pagoId, Long movimientoId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE venta_pago SET movimiento_caja_id=?, updated_at=datetime('now') WHERE id=?")) {
            statement.setLong(1, movimientoId);
            statement.setLong(2, pagoId);
            statement.executeUpdate();
        }
    }

    private void recalcularCaja(Connection connection, Long cajaId) throws SQLException {
        String sql = """
                UPDATE caja_diaria
                SET total_ingresos = COALESCE((SELECT SUM(monto) FROM movimiento_caja WHERE caja_diaria_id=? AND tipo_movimiento='INGRESO'),0),
                    total_egresos = COALESCE((SELECT SUM(monto) FROM movimiento_caja WHERE caja_diaria_id=? AND tipo_movimiento='EGRESO'),0),
                    saldo_esperado = saldo_inicial
                        + COALESCE((SELECT SUM(monto) FROM movimiento_caja WHERE caja_diaria_id=? AND tipo_movimiento='INGRESO'),0)
                        - COALESCE((SELECT SUM(monto) FROM movimiento_caja WHERE caja_diaria_id=? AND tipo_movimiento='EGRESO'),0),
                    updated_at = datetime('now')
                WHERE id=?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cajaId);
            statement.setLong(2, cajaId);
            statement.setLong(3, cajaId);
            statement.setLong(4, cajaId);
            statement.setLong(5, cajaId);
            statement.executeUpdate();
        }
    }

    private MetodoPagoCaja metodo(MetodoPagoCaja metodo) { return metodo == null ? MetodoPagoCaja.EFECTIVO : metodo; }
    private BigDecimal nvl(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private String blank(String value) { return value == null || value.isBlank() ? null : value.strip(); }

    private record CuentaCobrarRow(Long id, BigDecimal saldoPendiente, String estado) { }
    private record CuentaPagarRow(Long id, BigDecimal saldoPendiente, String estado) { }
    private record VentaPagoRow(Long id, BigDecimal monto, String metodoPago) { }
}
