package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.fiscalidad;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.fiscalidad.FiscalidadPreparadaRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.CrearDocumentoFiscalPreparadoSolicitud;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.DocumentoFiscalPreparado;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.DocumentoFiscalPreparadoDetalle;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.EstadoDocumentoFiscalPreparado;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.ImpuestoConfiguracion;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.TipoComprobanteLocal;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.TipoIdentificacionLocal;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Adaptador SQLite para fiscalidad preparada local. */
public final class SqliteFiscalidadPreparadaRepository extends SqliteRepositorySupport implements FiscalidadPreparadaRepository {

    public SqliteFiscalidadPreparadaRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<TipoIdentificacionLocal> listarTiposIdentificacionActivos() {
        String sql = """
                SELECT codigo, nombre, descripcion, estado
                FROM tipo_identificacion_local
                WHERE estado = 'ACTIVO'
                ORDER BY nombre COLLATE NOCASE
                """;
        return jdbc.query(sql, statement -> {}, this::mapTipoIdentificacion, "No se pudieron listar los tipos de identificación.");
    }

    @Override
    public List<TipoComprobanteLocal> listarTiposComprobanteActivos() {
        String sql = """
                SELECT codigo, nombre, descripcion, requiere_tercero, advertencia_no_autorizado, estado
                FROM tipo_comprobante_local
                WHERE estado = 'ACTIVO'
                ORDER BY nombre COLLATE NOCASE
                """;
        return jdbc.query(sql, statement -> {}, this::mapTipoComprobante, "No se pudieron listar los tipos de comprobante local.");
    }

    @Override
    public List<ImpuestoConfiguracion> listarImpuestosActivos() {
        String sql = """
                SELECT id, codigo, nombre, porcentaje, fecha_inicio, fecha_fin,
                       aplica_ventas, aplica_compras, estado, observacion
                FROM impuesto_configuracion
                WHERE estado = 'ACTIVO'
                ORDER BY nombre COLLATE NOCASE
                """;
        return jdbc.query(sql, statement -> {}, this::mapImpuesto, "No se pudieron listar los impuestos configurados.");
    }

    @Override
    public Optional<DocumentoFiscalPreparado> buscarDocumentoPorId(long id) {
        String sql = """
                SELECT id, tipo_comprobante_codigo, tercero_id, venta_interna_id, compra_id,
                       secuencia, fecha_emision, estado, subtotal, impuesto_total, total,
                       advertencia_no_autorizado, observacion
                FROM documento_fiscal_preparado
                WHERE id = ?
                """;
        return jdbc.queryOne(sql, statement -> statement.setLong(1, id), rs -> mapDocumento(rs, listarDetalles(id)), "No se pudo leer el documento preparado.");
    }

    @Override
    public DocumentoFiscalPreparado crearDocumentoPreparado(CrearDocumentoFiscalPreparadoSolicitud solicitud) {
        return transactions.inTransaction(connection -> {
            TotalesDocumento totales = calcularTotales(solicitud.detalles());
            String tipo = solicitud.tipoComprobanteCodigo().strip().toUpperCase();
            String secuencia = normalizarSecuencia(solicitud.secuencia(), tipo);
            String advertencia = leerAdvertencia(tipo);

            long documentoId;
            String sqlDocumento = """
                    INSERT INTO documento_fiscal_preparado (
                        tipo_comprobante_codigo, tercero_id, venta_interna_id, compra_id, secuencia,
                        estado, subtotal, impuesto_total, total, advertencia_no_autorizado, observacion, updated_at
                    ) VALUES (?, ?, ?, ?, ?, 'PREPARADO', ?, ?, ?, ?, ?, datetime('now'))
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sqlDocumento, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, tipo);
                setNullableLong(statement, 2, solicitud.terceroId());
                setNullableLong(statement, 3, solicitud.ventaInternaId());
                setNullableLong(statement, 4, solicitud.compraId());
                statement.setString(5, secuencia);
                statement.setBigDecimal(6, totales.subtotal());
                statement.setBigDecimal(7, totales.impuestoTotal());
                statement.setBigDecimal(8, totales.total());
                statement.setString(9, advertencia);
                statement.setString(10, blankToNull(solicitud.observacion()));
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No se obtuvo id del documento preparado.");
                    }
                    documentoId = keys.getLong(1);
                }
            }

            String sqlDetalle = """
                    INSERT INTO documento_fiscal_preparado_detalle (
                        documento_id, producto_id, descripcion, cantidad, precio_unitario,
                        base_imponible, impuesto_id, valor_impuesto, total_linea, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sqlDetalle)) {
                for (DocumentoFiscalPreparadoDetalle detalle : solicitud.detalles()) {
                    BigDecimal base = baseImponible(detalle);
                    BigDecimal impuesto = zeroIfNull(detalle.valorImpuesto());
                    BigDecimal totalLinea = detalle.totalLinea() == null ? base.add(impuesto) : detalle.totalLinea();
                    statement.setLong(1, documentoId);
                    setNullableLong(statement, 2, detalle.productoId());
                    statement.setString(3, detalle.descripcion().strip());
                    statement.setBigDecimal(4, detalle.cantidad());
                    statement.setBigDecimal(5, zeroIfNull(detalle.precioUnitario()));
                    statement.setBigDecimal(6, base);
                    setNullableLong(statement, 7, detalle.impuestoId());
                    statement.setBigDecimal(8, impuesto);
                    statement.setBigDecimal(9, totalLinea);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            return buscarDocumentoPorId(documentoId).orElseThrow(() -> new InfrastructureException("No se pudo recuperar el documento preparado creado."));
        }, "No se pudo crear el documento preparado.");
    }

    @Override
    public void anularDocumento(long documentoId, String motivo) {
        String sql = """
                UPDATE documento_fiscal_preparado
                SET estado = 'ANULADO', observacion = COALESCE(observacion || ' | ', '') || ?, updated_at = datetime('now')
                WHERE id = ?
                """;
        jdbc.update(sql, statement -> {
            statement.setString(1, "Anulado: " + motivo);
            statement.setLong(2, documentoId);
        }, "No se pudo anular el documento preparado.");
    }

    private String leerAdvertencia(String tipoComprobanteCodigo) throws SQLException {
        String sql = "SELECT advertencia_no_autorizado FROM tipo_comprobante_local WHERE codigo = ?";
        try (var connection = connectionFactory.openConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, tipoComprobanteCodigo);
            try (var rs = statement.executeQuery()) {
                if (rs.next()) {
                    return emptyIfNull(rs.getString("advertencia_no_autorizado"));
                }
            }
        }
        return "Documento interno/preparado. No reemplaza comprobante autorizado por el SRI.";
    }

    private List<DocumentoFiscalPreparadoDetalle> listarDetalles(long documentoId) {
        String sql = """
                SELECT id, documento_id, producto_id, descripcion, cantidad, precio_unitario,
                       base_imponible, impuesto_id, valor_impuesto, total_linea
                FROM documento_fiscal_preparado_detalle
                WHERE documento_id = ?
                ORDER BY id
                """;
        return jdbc.query(sql, statement -> statement.setLong(1, documentoId), this::mapDetalle, "No se pudieron listar los detalles del documento preparado.");
    }

    private TipoIdentificacionLocal mapTipoIdentificacion(ResultSet rs) throws SQLException {
        return new TipoIdentificacionLocal(
                rs.getString("codigo"),
                rs.getString("nombre"),
                emptyIfNull(rs.getString("descripcion")),
                "ACTIVO".equalsIgnoreCase(rs.getString("estado"))
        );
    }

    private TipoComprobanteLocal mapTipoComprobante(ResultSet rs) throws SQLException {
        return new TipoComprobanteLocal(
                rs.getString("codigo"),
                rs.getString("nombre"),
                emptyIfNull(rs.getString("descripcion")),
                rs.getInt("requiere_tercero") == 1,
                emptyIfNull(rs.getString("advertencia_no_autorizado")),
                "ACTIVO".equalsIgnoreCase(rs.getString("estado"))
        );
    }

    private ImpuestoConfiguracion mapImpuesto(ResultSet rs) throws SQLException {
        return new ImpuestoConfiguracion(
                rs.getLong("id"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                bigDecimalOrZero(rs, "porcentaje"),
                emptyIfNull(rs.getString("fecha_inicio")),
                emptyIfNull(rs.getString("fecha_fin")),
                rs.getInt("aplica_ventas") == 1,
                rs.getInt("aplica_compras") == 1,
                "ACTIVO".equalsIgnoreCase(rs.getString("estado")),
                emptyIfNull(rs.getString("observacion"))
        );
    }

    private DocumentoFiscalPreparado mapDocumento(ResultSet rs, List<DocumentoFiscalPreparadoDetalle> detalles) throws SQLException {
        return new DocumentoFiscalPreparado(
                rs.getLong("id"),
                rs.getString("tipo_comprobante_codigo"),
                nullableLong(rs, "tercero_id"),
                nullableLong(rs, "venta_interna_id"),
                nullableLong(rs, "compra_id"),
                rs.getString("secuencia"),
                rs.getString("fecha_emision"),
                EstadoDocumentoFiscalPreparado.fromDb(rs.getString("estado")),
                bigDecimalOrZero(rs, "subtotal"),
                bigDecimalOrZero(rs, "impuesto_total"),
                bigDecimalOrZero(rs, "total"),
                emptyIfNull(rs.getString("advertencia_no_autorizado")),
                emptyIfNull(rs.getString("observacion")),
                List.copyOf(detalles)
        );
    }

    private DocumentoFiscalPreparadoDetalle mapDetalle(ResultSet rs) throws SQLException {
        return new DocumentoFiscalPreparadoDetalle(
                rs.getLong("id"),
                rs.getLong("documento_id"),
                nullableLong(rs, "producto_id"),
                rs.getString("descripcion"),
                bigDecimalOrZero(rs, "cantidad"),
                bigDecimalOrZero(rs, "precio_unitario"),
                bigDecimalOrZero(rs, "base_imponible"),
                nullableLong(rs, "impuesto_id"),
                bigDecimalOrZero(rs, "valor_impuesto"),
                bigDecimalOrZero(rs, "total_linea")
        );
    }

    private TotalesDocumento calcularTotales(List<DocumentoFiscalPreparadoDetalle> detalles) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal impuesto = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (DocumentoFiscalPreparadoDetalle detalle : detalles) {
            BigDecimal base = baseImponible(detalle);
            BigDecimal valorImpuesto = zeroIfNull(detalle.valorImpuesto());
            BigDecimal totalLinea = detalle.totalLinea() == null ? base.add(valorImpuesto) : detalle.totalLinea();
            subtotal = subtotal.add(base);
            impuesto = impuesto.add(valorImpuesto);
            total = total.add(totalLinea);
        }
        return new TotalesDocumento(subtotal, impuesto, total);
    }

    private BigDecimal baseImponible(DocumentoFiscalPreparadoDetalle detalle) {
        if (detalle.baseImponible() != null) {
            return detalle.baseImponible();
        }
        return detalle.cantidad().multiply(zeroIfNull(detalle.precioUnitario()));
    }

    private String normalizarSecuencia(String secuencia, String tipo) {
        if (secuencia != null && !secuencia.isBlank()) {
            return secuencia.strip();
        }
        return tipo + "-PREP-" + System.currentTimeMillis();
    }

    private record TotalesDocumento(BigDecimal subtotal, BigDecimal impuestoTotal, BigDecimal total) {}
}
