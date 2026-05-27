package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.caja;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.caja.CajaDiariaRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja.*;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Adaptador SQLite para caja diaria, gastos y arqueos locales. */
public final class SqliteCajaDiariaRepository extends SqliteRepositorySupport implements CajaDiariaRepository {
    public SqliteCajaDiariaRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    public Optional<CajaDiaria> findByFecha(LocalDate fecha) {
        try (Connection c = connectionFactory.openConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM caja_diaria WHERE fecha=?")) {
            s.setString(1, fecha.toString());
            try (ResultSet rs = s.executeQuery()) {
                return rs.next() ? Optional.of(mapCaja(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new InfrastructureException("No se pudo consultar la caja diaria.", e);
        }
    }

    public List<CajaDiaria> findRecent(int limit) {
        try (Connection c = connectionFactory.openConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM caja_diaria ORDER BY fecha DESC LIMIT ?")) {
            s.setInt(1, Math.max(1, limit));
            try (ResultSet rs = s.executeQuery()) {
                List<CajaDiaria> out = new ArrayList<>();
                while (rs.next()) out.add(mapCaja(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new InfrastructureException("No se pudo listar cajas diarias.", e);
        }
    }

    public List<MovimientoCaja> findMovimientos(Long cajaDiariaId) {
        if (cajaDiariaId == null) return List.of();
        try (Connection c = connectionFactory.openConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM movimiento_caja WHERE caja_diaria_id=? ORDER BY fecha_movimiento DESC,id DESC")) {
            s.setLong(1, cajaDiariaId);
            try (ResultSet rs = s.executeQuery()) {
                List<MovimientoCaja> out = new ArrayList<>();
                while (rs.next()) out.add(mapMov(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new InfrastructureException("No se pudieron listar movimientos de caja.", e);
        }
    }

    public List<TipoGasto> findTiposGastoActivos() {
        try (Connection c = connectionFactory.openConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM tipo_gasto WHERE activo=1 ORDER BY nombre COLLATE NOCASE")) {
            try (ResultSet rs = s.executeQuery()) {
                List<TipoGasto> out = new ArrayList<>();
                while (rs.next()) out.add(new TipoGasto(rs.getLong("id"), rs.getString("nombre"), rs.getString("descripcion"), rs.getInt("activo") == 1));
                return out;
            }
        } catch (SQLException e) {
            throw new InfrastructureException("No se pudieron listar tipos de gasto.", e);
        }
    }

    public List<GastoOperativo> findGastos(Long cajaDiariaId) {
        if (cajaDiariaId == null) return List.of();
        try (Connection c = connectionFactory.openConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM gasto_operativo WHERE caja_diaria_id=? ORDER BY fecha_gasto DESC,id DESC")) {
            s.setLong(1, cajaDiariaId);
            try (ResultSet rs = s.executeQuery()) {
                List<GastoOperativo> out = new ArrayList<>();
                while (rs.next()) out.add(mapGasto(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new InfrastructureException("No se pudieron listar gastos operativos.", e);
        }
    }

    public List<ArqueoCaja> findArqueos(Long cajaDiariaId) {
        if (cajaDiariaId == null) return List.of();
        try (Connection c = connectionFactory.openConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM arqueo_caja WHERE caja_diaria_id=? ORDER BY fecha_arqueo DESC,id DESC")) {
            s.setLong(1, cajaDiariaId);
            try (ResultSet rs = s.executeQuery()) {
                List<ArqueoCaja> out = new ArrayList<>();
                while (rs.next()) out.add(mapArqueo(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new InfrastructureException("No se pudieron listar arqueos de caja.", e);
        }
    }

    public CajaDiaria abrir(LocalDate fecha, BigDecimal saldoInicial, String observacion) {
        try (Connection c = connectionFactory.openConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO caja_diaria(fecha,saldo_inicial,saldo_esperado,observacion,updated_at) VALUES(?,?,?,?,datetime('now'))", Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, fecha.toString());
            s.setBigDecimal(2, saldoInicial);
            s.setBigDecimal(3, saldoInicial);
            s.setString(4, blank(observacion));
            s.executeUpdate();
            try (ResultSet k = s.getGeneratedKeys()) {
                if (k.next()) return findById(k.getLong(1));
            }
            throw new InfrastructureException("No se pudo obtener el ID de la caja creada.");
        } catch (SQLException e) {
            throw new InfrastructureException("No se pudo abrir la caja diaria.", e);
        }
    }

    public MovimientoCaja registrarMovimiento(RegistroMovimientoCaja m) {
        try (Connection c = connectionFactory.openConnection()) {
            c.setAutoCommit(false);
            try {
                CajaDiaria caja = findById(c, m.cajaDiariaId());
                if (caja.estado() != EstadoCajaDiaria.ABIERTA) throw new ValidationException("Solo se pueden registrar movimientos en caja abierta.");
                long id = insertarMovimiento(c, m.cajaDiariaId(), m.tipoMovimiento(), "MANUAL", null, m.monto(), m.metodoPago(), m.descripcion());
                recalcular(c, m.cajaDiariaId());
                c.commit();
                return findMovimientoById(id);
            } catch (RuntimeException | SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (ValidationException e) {
            throw e;
        } catch (SQLException e) {
            throw new InfrastructureException("No se pudo registrar el movimiento de caja.", e);
        }
    }

    public GastoOperativo registrarGasto(RegistroGastoOperativo gasto) {
        try (Connection c = connectionFactory.openConnection()) {
            c.setAutoCommit(false);
            try {
                CajaDiaria caja = findById(c, gasto.cajaDiariaId());
                if (caja.estado() != EstadoCajaDiaria.ABIERTA) throw new ValidationException("Solo se pueden registrar gastos en caja abierta.");
                validarTipoGastoActivo(c, gasto.tipoGastoId());
                long movimientoId = insertarMovimiento(c, gasto.cajaDiariaId(), TipoMovimientoCaja.EGRESO, "GASTO_OPERATIVO", null, gasto.monto(), gasto.formaPago(), gasto.descripcion());
                long gastoId;
                try (PreparedStatement s = c.prepareStatement("INSERT INTO gasto_operativo(caja_diaria_id,tipo_gasto_id,movimiento_caja_id,monto,forma_pago_codigo,descripcion,referencia,observacion,updated_at) VALUES(?,?,?,?,?,?,?,?,datetime('now'))", Statement.RETURN_GENERATED_KEYS)) {
                    s.setLong(1, gasto.cajaDiariaId());
                    s.setLong(2, gasto.tipoGastoId());
                    s.setLong(3, movimientoId);
                    s.setBigDecimal(4, gasto.monto());
                    s.setString(5, gasto.formaPago().dbValue());
                    s.setString(6, gasto.descripcion().strip());
                    s.setString(7, blank(gasto.referencia()));
                    s.setString(8, blank(gasto.observacion()));
                    s.executeUpdate();
                    try (ResultSet k = s.getGeneratedKeys()) {
                        if (!k.next()) throw new InfrastructureException("No se pudo obtener el ID del gasto operativo.");
                        gastoId = k.getLong(1);
                    }
                }
                try (PreparedStatement s = c.prepareStatement("UPDATE movimiento_caja SET referencia_id=?, updated_at=datetime('now') WHERE id=?")) {
                    s.setLong(1, gastoId);
                    s.setLong(2, movimientoId);
                    s.executeUpdate();
                }
                recalcular(c, gasto.cajaDiariaId());
                c.commit();
                return findGastoById(gastoId);
            } catch (RuntimeException | SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (ValidationException e) {
            throw e;
        } catch (SQLException e) {
            throw new InfrastructureException("No se pudo registrar el gasto operativo.", e);
        }
    }

    public ArqueoCaja registrarArqueo(RegistroArqueoCaja arqueo) {
        try (Connection c = connectionFactory.openConnection()) {
            CajaDiaria caja = findById(c, arqueo.cajaDiariaId());
            if (caja.estado() != EstadoCajaDiaria.ABIERTA) throw new ValidationException("Solo se puede arquear una caja abierta.");
            BigDecimal diferencia = arqueo.saldoContado().subtract(caja.saldoEsperado());
            try (PreparedStatement s = c.prepareStatement("INSERT INTO arqueo_caja(caja_diaria_id,saldo_sistema,saldo_contado,diferencia,responsable_texto,observacion,updated_at) VALUES(?,?,?,?,?,?,datetime('now'))", Statement.RETURN_GENERATED_KEYS)) {
                s.setLong(1, arqueo.cajaDiariaId());
                s.setBigDecimal(2, caja.saldoEsperado());
                s.setBigDecimal(3, arqueo.saldoContado());
                s.setBigDecimal(4, diferencia);
                s.setString(5, blank(arqueo.responsableTexto()));
                s.setString(6, blank(arqueo.observacion()));
                s.executeUpdate();
                try (ResultSet k = s.getGeneratedKeys()) {
                    if (k.next()) return findArqueoById(k.getLong(1));
                }
                throw new InfrastructureException("No se pudo obtener el ID del arqueo.");
            }
        } catch (ValidationException e) {
            throw e;
        } catch (SQLException e) {
            throw new InfrastructureException("No se pudo registrar el arqueo de caja.", e);
        }
    }

    public CajaDiaria cerrar(Long id, BigDecimal saldoContado, String observacion) {
        try (Connection c = connectionFactory.openConnection()) {
            c.setAutoCommit(false);
            try {
                CajaDiaria caja = findById(c, id);
                if (caja.estado() != EstadoCajaDiaria.ABIERTA) throw new ValidationException("Solo se puede cerrar una caja abierta.");
                BigDecimal dif = saldoContado.subtract(caja.saldoEsperado());
                try (PreparedStatement s = c.prepareStatement("UPDATE caja_diaria SET saldo_contado=?, diferencia=?, estado='CERRADA', observacion=?, updated_at=datetime('now') WHERE id=?")) {
                    s.setBigDecimal(1, saldoContado);
                    s.setBigDecimal(2, dif);
                    s.setString(3, blank(observacion));
                    s.setLong(4, id);
                    s.executeUpdate();
                }
                c.commit();
                return findById(id);
            } catch (RuntimeException | SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (ValidationException e) {
            throw e;
        } catch (SQLException e) {
            throw new InfrastructureException("No se pudo cerrar la caja diaria.", e);
        }
    }

    private long insertarMovimiento(Connection c, Long cajaId, TipoMovimientoCaja tipo, String origen, Long referenciaId, BigDecimal monto, MetodoPagoCaja metodo, String descripcion) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("INSERT INTO movimiento_caja(caja_diaria_id,tipo_movimiento,origen,referencia_id,monto,metodo_pago,descripcion,updated_at) VALUES(?,?,?,?,?,?,?,datetime('now'))", Statement.RETURN_GENERATED_KEYS)) {
            s.setLong(1, cajaId);
            s.setString(2, tipo.dbValue());
            s.setString(3, origen);
            if (referenciaId == null) s.setNull(4, Types.INTEGER); else s.setLong(4, referenciaId);
            s.setBigDecimal(5, monto);
            s.setString(6, metodo.dbValue());
            s.setString(7, descripcion.strip());
            s.executeUpdate();
            try (ResultSet k = s.getGeneratedKeys()) {
                if (!k.next()) throw new InfrastructureException("No se pudo obtener el ID del movimiento.");
                return k.getLong(1);
            }
        }
    }

    private void recalcular(Connection c, Long id) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("UPDATE caja_diaria SET total_ingresos=COALESCE((SELECT SUM(monto) FROM movimiento_caja WHERE caja_diaria_id=? AND tipo_movimiento='INGRESO'),0), total_egresos=COALESCE((SELECT SUM(monto) FROM movimiento_caja WHERE caja_diaria_id=? AND tipo_movimiento='EGRESO'),0), saldo_esperado=saldo_inicial+COALESCE((SELECT SUM(monto) FROM movimiento_caja WHERE caja_diaria_id=? AND tipo_movimiento='INGRESO'),0)-COALESCE((SELECT SUM(monto) FROM movimiento_caja WHERE caja_diaria_id=? AND tipo_movimiento='EGRESO'),0), updated_at=datetime('now') WHERE id=?")) {
            s.setLong(1, id); s.setLong(2, id); s.setLong(3, id); s.setLong(4, id); s.setLong(5, id); s.executeUpdate();
        }
    }

    private void validarTipoGastoActivo(Connection c, Long tipoGastoId) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT activo FROM tipo_gasto WHERE id=?")) {
            s.setLong(1, tipoGastoId);
            try (ResultSet rs = s.executeQuery()) {
                if (!rs.next()) throw new ValidationException("El tipo de gasto seleccionado no existe.");
                if (rs.getInt("activo") != 1) throw new ValidationException("El tipo de gasto seleccionado está inactivo.");
            }
        }
    }

    private CajaDiaria findById(long id) throws SQLException { try (Connection c = connectionFactory.openConnection()) { return findById(c, id); } }
    private CajaDiaria findById(Connection c, long id) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT * FROM caja_diaria WHERE id=?")) {
            s.setLong(1, id);
            try (ResultSet rs = s.executeQuery()) { if (rs.next()) return mapCaja(rs); }
        }
        throw new ValidationException("La caja diaria seleccionada no existe.");
    }

    private MovimientoCaja findMovimientoById(long id) throws SQLException {
        try (Connection c = connectionFactory.openConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM movimiento_caja WHERE id=?")) {
            s.setLong(1, id);
            try (ResultSet rs = s.executeQuery()) { if (rs.next()) return mapMov(rs); }
        }
        throw new InfrastructureException("No se pudo leer el movimiento creado.");
    }

    private GastoOperativo findGastoById(long id) throws SQLException {
        try (Connection c = connectionFactory.openConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM gasto_operativo WHERE id=?")) {
            s.setLong(1, id);
            try (ResultSet rs = s.executeQuery()) { if (rs.next()) return mapGasto(rs); }
        }
        throw new InfrastructureException("No se pudo leer el gasto creado.");
    }

    private ArqueoCaja findArqueoById(long id) throws SQLException {
        try (Connection c = connectionFactory.openConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM arqueo_caja WHERE id=?")) {
            s.setLong(1, id);
            try (ResultSet rs = s.executeQuery()) { if (rs.next()) return mapArqueo(rs); }
        }
        throw new InfrastructureException("No se pudo leer el arqueo creado.");
    }

    private CajaDiaria mapCaja(ResultSet rs) throws SQLException {
        return new CajaDiaria(rs.getLong("id"), LocalDate.parse(rs.getString("fecha")), nvl(rs.getBigDecimal("saldo_inicial")), nvl(rs.getBigDecimal("total_ingresos")), nvl(rs.getBigDecimal("total_egresos")), nvl(rs.getBigDecimal("saldo_esperado")), rs.getBigDecimal("saldo_contado"), nvl(rs.getBigDecimal("diferencia")), EstadoCajaDiaria.fromDb(rs.getString("estado")), rs.getString("observacion"));
    }

    private MovimientoCaja mapMov(ResultSet rs) throws SQLException {
        String f = rs.getString("fecha_movimiento");
        long ref = rs.getLong("referencia_id");
        return new MovimientoCaja(rs.getLong("id"), rs.getLong("caja_diaria_id"), TipoMovimientoCaja.fromDb(rs.getString("tipo_movimiento")), rs.getString("origen"), rs.wasNull() ? null : ref, nvl(rs.getBigDecimal("monto")), MetodoPagoCaja.fromDb(rs.getString("metodo_pago")), rs.getString("descripcion"), parseDateTime(f));
    }

    private GastoOperativo mapGasto(ResultSet rs) throws SQLException {
        long mov = rs.getLong("movimiento_caja_id");
        return new GastoOperativo(rs.getLong("id"), rs.getLong("caja_diaria_id"), rs.getLong("tipo_gasto_id"), rs.wasNull() ? null : mov, parseDateTime(rs.getString("fecha_gasto")), nvl(rs.getBigDecimal("monto")), MetodoPagoCaja.fromDb(rs.getString("forma_pago_codigo")), rs.getString("descripcion"), rs.getString("referencia"), rs.getString("observacion"));
    }

    private ArqueoCaja mapArqueo(ResultSet rs) throws SQLException {
        return new ArqueoCaja(rs.getLong("id"), rs.getLong("caja_diaria_id"), parseDateTime(rs.getString("fecha_arqueo")), nvl(rs.getBigDecimal("saldo_sistema")), nvl(rs.getBigDecimal("saldo_contado")), nvl(rs.getBigDecimal("diferencia")), rs.getString("responsable_texto"), rs.getString("observacion"));
    }

    private LocalDateTime parseDateTime(String value) { return value == null ? null : LocalDateTime.parse(value.replace(' ', 'T')); }
    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private String blank(String v) { return v == null || v.isBlank() ? null : v.strip(); }
}
