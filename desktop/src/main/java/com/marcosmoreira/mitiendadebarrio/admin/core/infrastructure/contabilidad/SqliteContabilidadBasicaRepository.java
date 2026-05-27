package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.contabilidad;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.contabilidad.ContabilidadBasicaRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.AsientoContable;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.AsientoContableDetalle;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.AsientoContableDetalleSolicitud;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.CrearAsientoContableSolicitud;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.CuentaContable;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.EstadoAsientoContable;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.LadoPlantillaAsiento;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.PlantillaAsiento;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.PlantillaAsientoDetalle;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.ReglaContableEvento;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.TipoCuentaContable;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.TipoDiarioContable;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Adaptador SQLite para contabilidad básica local. */
public final class SqliteContabilidadBasicaRepository extends SqliteRepositorySupport implements ContabilidadBasicaRepository {

    public SqliteContabilidadBasicaRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<CuentaContable> listarCuentasActivas() {
        String sql = """
                SELECT id, codigo, nombre, tipo, cuenta_padre_id, imputable, activa
                FROM cuenta_contable
                WHERE activa = 1
                ORDER BY codigo COLLATE NOCASE
                """;
        return jdbc.query(sql, statement -> {}, this::mapCuenta, "No se pudieron listar las cuentas contables.");
    }

    @Override
    public List<TipoDiarioContable> listarTiposDiarioActivos() {
        String sql = """
                SELECT codigo, nombre, activo
                FROM tipo_diario_contable
                WHERE activo = 1
                ORDER BY nombre COLLATE NOCASE
                """;
        return jdbc.query(sql, statement -> {}, this::mapTipoDiario, "No se pudieron listar los tipos de diario contable.");
    }

    @Override
    public List<PlantillaAsiento> listarPlantillasActivas() {
        String sql = """
                SELECT id, codigo, nombre, descripcion, activo
                FROM plantilla_asiento
                WHERE activo = 1
                ORDER BY nombre COLLATE NOCASE
                """;
        return jdbc.query(sql, statement -> {}, rs -> mapPlantilla(rs, listarDetallesPlantilla(rs.getLong("id"))),
                "No se pudieron listar las plantillas contables.");
    }

    @Override
    public List<ReglaContableEvento> listarReglasActivas() {
        String sql = """
                SELECT id, evento_codigo, plantilla_id, descripcion, activo
                FROM regla_contable_evento
                WHERE activo = 1
                ORDER BY evento_codigo COLLATE NOCASE
                """;
        return jdbc.query(sql, statement -> {}, this::mapRegla, "No se pudieron listar las reglas contables.");
    }

    @Override
    public Optional<PlantillaAsiento> buscarPlantillaPorCodigo(String codigo) {
        String sql = """
                SELECT id, codigo, nombre, descripcion, activo
                FROM plantilla_asiento
                WHERE UPPER(codigo) = UPPER(?) AND activo = 1
                """;
        return jdbc.queryOne(sql, statement -> statement.setString(1, normalize(codigo)),
                rs -> mapPlantilla(rs, listarDetallesPlantilla(rs.getLong("id"))),
                "No se pudo buscar la plantilla contable.");
    }

    @Override
    public Optional<ReglaContableEvento> buscarReglaActivaPorEvento(String eventoCodigo) {
        String sql = """
                SELECT id, evento_codigo, plantilla_id, descripcion, activo
                FROM regla_contable_evento
                WHERE UPPER(evento_codigo) = UPPER(?) AND activo = 1
                """;
        return jdbc.queryOne(sql, statement -> statement.setString(1, normalize(eventoCodigo)),
                this::mapRegla, "No se pudo buscar la regla contable.");
    }

    @Override
    public Optional<AsientoContable> buscarAsientoPorId(long id) {
        String sql = """
                SELECT id, numero_asiento, tipo_diario_codigo, fecha_asiento, periodo_anio, periodo_mes,
                       concepto, estado, origen_tipo, origen_id, total_debe, total_haber
                FROM asiento_contable
                WHERE id = ?
                """;
        return jdbc.queryOne(sql, statement -> statement.setLong(1, id), rs -> mapAsiento(rs, listarDetalles(id)), "No se pudo leer el asiento contable.");
    }

    @Override
    public List<AsientoContable> listarAsientosRecientes(int limite) {
        String sql = """
                SELECT id, numero_asiento, tipo_diario_codigo, fecha_asiento, periodo_anio, periodo_mes,
                       concepto, estado, origen_tipo, origen_id, total_debe, total_haber
                FROM asiento_contable
                ORDER BY fecha_asiento DESC, id DESC
                LIMIT ?
                """;
        return jdbc.query(sql, statement -> statement.setInt(1, limite), rs -> mapAsiento(rs, List.of()), "No se pudieron listar los asientos recientes.");
    }

    @Override
    public AsientoContable registrarAsiento(CrearAsientoContableSolicitud solicitud) {
        return transactions.inTransaction(connection -> {
            String tipoDiario = solicitud.tipoDiarioCodigo() == null || solicitud.tipoDiarioCodigo().isBlank()
                    ? "GENERAL"
                    : solicitud.tipoDiarioCodigo().strip().toUpperCase();
            String fecha = solicitud.fechaAsiento() == null || solicitud.fechaAsiento().isBlank()
                    ? LocalDate.now().toString()
                    : solicitud.fechaAsiento().strip();
            LocalDate localDate = LocalDate.parse(fecha);
            BigDecimal totalDebe = totalDebe(solicitud.detalles());
            BigDecimal totalHaber = totalHaber(solicitud.detalles());
            String numero = siguienteNumero(connection, localDate.getYear(), localDate.getMonthValue());

            String sqlAsiento = """
                    INSERT INTO asiento_contable (
                        numero_asiento, tipo_diario_codigo, fecha_asiento, periodo_anio, periodo_mes,
                        concepto, estado, origen_tipo, origen_id, total_debe, total_haber, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, 'REGISTRADO', ?, ?, ?, ?, datetime('now'))
                    """;
            long asientoId;
            try (PreparedStatement statement = connection.prepareStatement(sqlAsiento, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, numero);
                statement.setString(2, tipoDiario);
                statement.setString(3, fecha);
                statement.setInt(4, localDate.getYear());
                statement.setInt(5, localDate.getMonthValue());
                statement.setString(6, solicitud.concepto().strip());
                statement.setString(7, blankToNull(solicitud.origenTipo()));
                setNullableLong(statement, 8, solicitud.origenId());
                statement.setBigDecimal(9, totalDebe);
                statement.setBigDecimal(10, totalHaber);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No se obtuvo id del asiento contable.");
                    }
                    asientoId = keys.getLong(1);
                }
            }

            String sqlDetalle = """
                    INSERT INTO asiento_contable_detalle (
                        asiento_id, cuenta_id, linea, descripcion, debe, haber, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, datetime('now'))
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sqlDetalle)) {
                int linea = 1;
                for (AsientoContableDetalleSolicitud detalle : solicitud.detalles()) {
                    statement.setLong(1, asientoId);
                    statement.setLong(2, detalle.cuentaId());
                    statement.setInt(3, linea++);
                    statement.setString(4, blankToNull(detalle.descripcion()));
                    statement.setBigDecimal(5, zeroIfNull(detalle.debe()));
                    statement.setBigDecimal(6, zeroIfNull(detalle.haber()));
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            return buscarAsientoPorId(asientoId).orElseThrow(() -> new InfrastructureException("No se pudo recuperar el asiento contable creado."));
        }, "No se pudo registrar el asiento contable.");
    }

    @Override
    public void anularAsiento(long asientoId, String motivo) {
        String sql = """
                UPDATE asiento_contable
                SET estado = 'ANULADO', concepto = concepto || ' | Anulado: ' || ?, updated_at = datetime('now')
                WHERE id = ? AND estado <> 'ANULADO'
                """;
        jdbc.update(sql, statement -> {
            statement.setString(1, motivo);
            statement.setLong(2, asientoId);
        }, "No se pudo anular el asiento contable.");
    }

    private String siguienteNumero(java.sql.Connection connection, int anio, int mes) throws SQLException {
        String prefijo = "ASI-" + anio + String.format("%02d", mes) + "-";
        String sql = "SELECT COUNT(*) + 1 AS siguiente FROM asiento_contable WHERE periodo_anio = ? AND periodo_mes = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, anio);
            statement.setInt(2, mes);
            try (ResultSet rs = statement.executeQuery()) {
                int siguiente = rs.next() ? rs.getInt("siguiente") : 1;
                return prefijo + String.format("%05d", siguiente);
            }
        }
    }

    private List<PlantillaAsientoDetalle> listarDetallesPlantilla(long plantillaId) {
        String sql = """
                SELECT id, plantilla_id, cuenta_id, linea, lado, descripcion
                FROM plantilla_asiento_detalle
                WHERE plantilla_id = ?
                ORDER BY linea
                """;
        return jdbc.query(sql, statement -> statement.setLong(1, plantillaId), this::mapPlantillaDetalle,
                "No se pudieron listar los detalles de la plantilla contable.");
    }

    private List<AsientoContableDetalle> listarDetalles(long asientoId) {
        String sql = """
                SELECT id, asiento_id, cuenta_id, linea, descripcion, debe, haber
                FROM asiento_contable_detalle
                WHERE asiento_id = ?
                ORDER BY linea
                """;
        return jdbc.query(sql, statement -> statement.setLong(1, asientoId), this::mapDetalle, "No se pudieron listar los detalles del asiento.");
    }

    private PlantillaAsiento mapPlantilla(ResultSet rs, List<PlantillaAsientoDetalle> detalles) throws SQLException {
        return new PlantillaAsiento(
                rs.getLong("id"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                emptyIfNull(rs.getString("descripcion")),
                rs.getInt("activo") == 1,
                List.copyOf(detalles)
        );
    }

    private PlantillaAsientoDetalle mapPlantillaDetalle(ResultSet rs) throws SQLException {
        return new PlantillaAsientoDetalle(
                rs.getLong("id"),
                rs.getLong("plantilla_id"),
                rs.getLong("cuenta_id"),
                rs.getInt("linea"),
                LadoPlantillaAsiento.fromDb(rs.getString("lado")),
                emptyIfNull(rs.getString("descripcion"))
        );
    }

    private ReglaContableEvento mapRegla(ResultSet rs) throws SQLException {
        return new ReglaContableEvento(
                rs.getLong("id"),
                rs.getString("evento_codigo"),
                nullableLong(rs, "plantilla_id"),
                emptyIfNull(rs.getString("descripcion")),
                rs.getInt("activo") == 1
        );
    }

    private CuentaContable mapCuenta(ResultSet rs) throws SQLException {
        return new CuentaContable(
                rs.getLong("id"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                TipoCuentaContable.fromDb(rs.getString("tipo")),
                nullableLong(rs, "cuenta_padre_id"),
                rs.getInt("imputable") == 1,
                rs.getInt("activa") == 1
        );
    }

    private TipoDiarioContable mapTipoDiario(ResultSet rs) throws SQLException {
        return new TipoDiarioContable(rs.getString("codigo"), rs.getString("nombre"), rs.getInt("activo") == 1);
    }

    private AsientoContable mapAsiento(ResultSet rs, List<AsientoContableDetalle> detalles) throws SQLException {
        return new AsientoContable(
                rs.getLong("id"),
                rs.getString("numero_asiento"),
                rs.getString("tipo_diario_codigo"),
                rs.getString("fecha_asiento"),
                rs.getInt("periodo_anio"),
                rs.getInt("periodo_mes"),
                rs.getString("concepto"),
                EstadoAsientoContable.fromDb(rs.getString("estado")),
                emptyIfNull(rs.getString("origen_tipo")),
                nullableLong(rs, "origen_id"),
                bigDecimalOrZero(rs, "total_debe"),
                bigDecimalOrZero(rs, "total_haber"),
                List.copyOf(detalles)
        );
    }

    private AsientoContableDetalle mapDetalle(ResultSet rs) throws SQLException {
        return new AsientoContableDetalle(
                rs.getLong("id"),
                rs.getLong("asiento_id"),
                rs.getLong("cuenta_id"),
                rs.getInt("linea"),
                emptyIfNull(rs.getString("descripcion")),
                bigDecimalOrZero(rs, "debe"),
                bigDecimalOrZero(rs, "haber")
        );
    }

    private BigDecimal totalDebe(List<AsientoContableDetalleSolicitud> detalles) {
        BigDecimal total = BigDecimal.ZERO;
        for (AsientoContableDetalleSolicitud detalle : detalles) {
            total = total.add(zeroIfNull(detalle.debe()));
        }
        return total;
    }

    private BigDecimal totalHaber(List<AsientoContableDetalleSolicitud> detalles) {
        BigDecimal total = BigDecimal.ZERO;
        for (AsientoContableDetalleSolicitud detalle : detalles) {
            total = total.add(zeroIfNull(detalle.haber()));
        }
        return total;
    }
}
