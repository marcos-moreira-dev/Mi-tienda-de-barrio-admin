package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.fiado;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.fiado.FiadoRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja.MetodoPagoCaja;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiado.*;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/** Adaptador SQLite para fiado/cuentas por cobrar. */
public final class SqliteFiadoRepository extends SqliteRepositorySupport implements FiadoRepository {
    public SqliteFiadoRepository(SqliteConnectionFactory connectionFactory) { super(connectionFactory); }

    public List<ClienteFiado> findClientes(String query, boolean incluirInactivos) {
        String normalized = query == null || query.isBlank() ? null : "%" + query.strip() + "%";
        String sql = """
                SELECT c.*, COALESCE((SELECT SUM(saldo_pendiente) FROM cuenta_por_cobrar cc
                    WHERE cc.cliente_fiado_id = c.id AND cc.estado = 'ABIERTA'), 0) AS saldo_pendiente
                FROM cliente_fiado c
                WHERE (? IS NULL OR c.nombre LIKE ? OR c.telefono LIKE ?) AND (? = 1 OR c.estado = 'ACTIVO')
                ORDER BY c.nombre COLLATE NOCASE
                """;
        try (Connection connection = connectionFactory.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalized);
            statement.setString(2, normalized);
            statement.setString(3, normalized);
            statement.setInt(4, incluirInactivos ? 1 : 0);
            try (ResultSet rs = statement.executeQuery()) {
                List<ClienteFiado> items = new ArrayList<>();
                while (rs.next()) items.add(mapCliente(rs));
                return items;
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudieron listar clientes de fiado.", ex);
        }
    }

    public ClienteFiado guardarCliente(ClienteFiado cliente) { return cliente.id() == null ? insertarCliente(cliente) : actualizarCliente(cliente); }

    public void cambiarEstadoCliente(Long clienteId, boolean activo) {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE cliente_fiado SET estado = ?, updated_at = datetime('now') WHERE id = ?")) {
            statement.setString(1, activo ? "ACTIVO" : "INACTIVO");
            statement.setLong(2, clienteId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo cambiar el estado del cliente de fiado.", ex);
        }
    }

    public List<CuentaPorCobrar> findCuentasAbiertas(Long clienteId) {
        if (clienteId == null) return List.of();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM cuenta_por_cobrar WHERE cliente_fiado_id = ? AND estado = 'ABIERTA' ORDER BY fecha_apertura DESC, id DESC")) {
            statement.setLong(1, clienteId);
            try (ResultSet rs = statement.executeQuery()) {
                List<CuentaPorCobrar> items = new ArrayList<>();
                while (rs.next()) items.add(mapCuenta(rs));
                return items;
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudieron listar cuentas por cobrar.", ex);
        }
    }

    public CuentaPorCobrar abrirCuenta(Long clienteId, BigDecimal monto, String observacion) {
        String sql = "INSERT INTO cuenta_por_cobrar (cliente_fiado_id, monto_original, saldo_pendiente, observacion, updated_at) VALUES (?, ?, ?, ?, datetime('now'))";
        try (Connection connection = connectionFactory.openConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, clienteId);
            statement.setBigDecimal(2, monto);
            statement.setBigDecimal(3, monto);
            statement.setString(4, blankToNull(observacion));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) return findCuentaById(keys.getLong(1)); }
            throw new InfrastructureException("No se pudo obtener el ID de la cuenta creada.");
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo abrir la cuenta por cobrar.", ex);
        }
    }

    public Abono registrarAbono(Long cuentaId, BigDecimal monto, MetodoPagoCaja metodoPago, String observacion) {
        try (Connection connection = connectionFactory.openConnection()) {
            connection.setAutoCommit(false);
            try {
                CuentaPorCobrar cuenta = findCuentaById(connection, cuentaId);
                if (cuenta.estado() != EstadoCuentaPorCobrar.ABIERTA) throw new ValidationException("Solo se puede abonar a cuentas abiertas.");
                if (monto.compareTo(cuenta.saldoPendiente()) > 0) throw new ValidationException("El abono no puede ser mayor que el saldo pendiente.");
                long abonoId = insertarAbono(connection, cuentaId, monto, metodoPago, observacion);
                BigDecimal nuevoSaldo = cuenta.saldoPendiente().subtract(monto);
                actualizarSaldoCuenta(connection, cuentaId, nuevoSaldo);
                connection.commit();
                return findAbonoById(abonoId);
            } catch (RuntimeException | SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (ValidationException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo registrar el abono.", ex);
        }
    }

    private ClienteFiado insertarCliente(ClienteFiado cliente) {
        String sql = "INSERT INTO cliente_fiado (nombre, telefono, direccion, limite_credito, estado, observacion, updated_at) VALUES (?, ?, ?, ?, ?, ?, datetime('now'))";
        try (Connection connection = connectionFactory.openConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillCliente(statement, cliente);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) return findClienteById(keys.getLong(1)); }
            throw new InfrastructureException("No se pudo obtener el ID del cliente creado.");
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo guardar el cliente de fiado.", ex);
        }
    }

    private ClienteFiado actualizarCliente(ClienteFiado cliente) {
        String sql = "UPDATE cliente_fiado SET nombre = ?, telefono = ?, direccion = ?, limite_credito = ?, estado = ?, observacion = ?, updated_at = datetime('now') WHERE id = ?";
        try (Connection connection = connectionFactory.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            fillCliente(statement, cliente);
            statement.setLong(7, cliente.id());
            statement.executeUpdate();
            return findClienteById(cliente.id());
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo actualizar el cliente de fiado.", ex);
        }
    }

    private void fillCliente(PreparedStatement statement, ClienteFiado cliente) throws SQLException {
        statement.setString(1, cliente.nombre().strip());
        statement.setString(2, blankToNull(cliente.telefono()));
        statement.setString(3, blankToNull(cliente.direccion()));
        statement.setBigDecimal(4, cliente.limiteCredito() == null ? BigDecimal.ZERO : cliente.limiteCredito());
        statement.setString(5, cliente.estado() == null ? "ACTIVO" : cliente.estado().dbValue());
        statement.setString(6, blankToNull(cliente.observacion()));
    }

    private long insertarAbono(Connection connection, Long cuentaId, BigDecimal monto, MetodoPagoCaja metodoPago, String observacion) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO abono (cuenta_por_cobrar_id, monto, metodo_pago, observacion, updated_at) VALUES (?, ?, ?, ?, datetime('now'))", Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, cuentaId);
            statement.setBigDecimal(2, monto);
            statement.setString(3, metodoPago.dbValue());
            statement.setString(4, blankToNull(observacion));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1); }
        }
        throw new InfrastructureException("No se pudo obtener el ID del abono creado.");
    }

    private void actualizarSaldoCuenta(Connection connection, Long cuentaId, BigDecimal nuevoSaldo) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE cuenta_por_cobrar SET saldo_pendiente = ?, estado = ?, updated_at = datetime('now') WHERE id = ?")) {
            statement.setBigDecimal(1, nuevoSaldo);
            statement.setString(2, nuevoSaldo.compareTo(BigDecimal.ZERO) == 0 ? "CERRADA" : "ABIERTA");
            statement.setLong(3, cuentaId);
            statement.executeUpdate();
        }
    }

    private ClienteFiado findClienteById(long id) throws SQLException {
        try (Connection connection = connectionFactory.openConnection(); PreparedStatement statement = connection.prepareStatement("""
                SELECT c.*, COALESCE((SELECT SUM(saldo_pendiente) FROM cuenta_por_cobrar cc
                    WHERE cc.cliente_fiado_id = c.id AND cc.estado = 'ABIERTA'), 0) AS saldo_pendiente
                FROM cliente_fiado c WHERE c.id = ?
                """)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) { if (rs.next()) return mapCliente(rs); }
        }
        throw new InfrastructureException("No se pudo leer el cliente guardado.");
    }

    private CuentaPorCobrar findCuentaById(long id) throws SQLException {
        try (Connection connection = connectionFactory.openConnection()) { return findCuentaById(connection, id); }
    }

    private CuentaPorCobrar findCuentaById(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM cuenta_por_cobrar WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) { if (rs.next()) return mapCuenta(rs); }
        }
        throw new ValidationException("La cuenta por cobrar seleccionada no existe.");
    }

    private Abono findAbonoById(long id) throws SQLException {
        try (Connection connection = connectionFactory.openConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM abono WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) { if (rs.next()) return mapAbono(rs); }
        }
        throw new InfrastructureException("No se pudo leer el abono registrado.");
    }

    private ClienteFiado mapCliente(ResultSet rs) throws SQLException {
        return new ClienteFiado(rs.getLong("id"), rs.getString("nombre"), rs.getString("telefono"), rs.getString("direccion"),
                nvl(rs.getBigDecimal("limite_credito")), EstadoClienteFiado.fromDb(rs.getString("estado")),
                rs.getString("observacion"), nvl(rs.getBigDecimal("saldo_pendiente")));
    }

    private CuentaPorCobrar mapCuenta(ResultSet rs) throws SQLException {
        String fecha = rs.getString("fecha_apertura");
        long ventaId = rs.getLong("venta_interna_id");
        return new CuentaPorCobrar(rs.getLong("id"), rs.getLong("cliente_fiado_id"), rs.wasNull() ? null : ventaId,
                fecha == null ? null : LocalDateTime.parse(fecha.replace(' ', 'T')),
                nvl(rs.getBigDecimal("monto_original")), nvl(rs.getBigDecimal("saldo_pendiente")),
                EstadoCuentaPorCobrar.fromDb(rs.getString("estado")), rs.getString("observacion"));
    }

    private Abono mapAbono(ResultSet rs) throws SQLException {
        String fecha = rs.getString("fecha_abono");
        return new Abono(rs.getLong("id"), rs.getLong("cuenta_por_cobrar_id"),
                fecha == null ? null : LocalDateTime.parse(fecha.replace(' ', 'T')),
                nvl(rs.getBigDecimal("monto")), MetodoPagoCaja.fromDb(rs.getString("metodo_pago")), rs.getString("observacion"));
    }

    private BigDecimal nvl(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
}
