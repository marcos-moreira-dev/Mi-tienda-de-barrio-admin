package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.reporte;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.reporte.ReporteOperativoRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.reporte.ReporteLinea;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.reporte.ReporteOperativo;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.reporte.TipoReporteOperativo;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Consultas SQLite para reportes operativos de tienda. */
public final class SqliteReporteOperativoRepository extends SqliteRepositorySupport implements ReporteOperativoRepository {

    public SqliteReporteOperativoRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public ReporteOperativo generar(TipoReporteOperativo tipo) {
        return switch (tipo) {
            case PRODUCTOS_POR_COMPRAR -> productosPorComprar();
            case BAJO_STOCK -> bajoStock();
            case AGOTADOS -> agotados();
            case PROXIMOS_A_VENCER -> proximosVencer();
            case INVENTARIO_VALORIZADO -> inventarioValorizado();
            case COMPRAS_RECIENTES -> comprasRecientes();
            case VENTAS_INTERNAS_RECIENTES -> ventasRecientes();
            case MERMAS_RETIROS_RECIENTES -> mermasRecientes();
            case CIERRE_CAJA_RECIENTE -> cierresCajaRecientes();
            case FIADO_PENDIENTE -> fiadoPendiente();
            case ABONOS_RECIENTES -> abonosRecientes();
            case GASTOS_OPERATIVOS -> gastosOperativos();
            case CUENTAS_POR_PAGAR -> cuentasPorPagar();
        };
    }

    private ReporteOperativo productosPorComprar() {
        String sql = """
                SELECT p.nombre, c.nombre AS categoria, p.stock_actual, p.stock_minimo,
                       COALESCE(p.stock_objetivo, p.stock_minimo) AS stock_objetivo,
                       CASE WHEN COALESCE(p.stock_objetivo, p.stock_minimo) - p.stock_actual > 0
                            THEN COALESCE(p.stock_objetivo, p.stock_minimo) - p.stock_actual ELSE 0 END AS sugerido,
                       COALESCE(pr.nombre, '') AS proveedor
                FROM producto p
                JOIN categoria c ON c.id = p.categoria_id
                LEFT JOIN proveedor pr ON pr.id = p.proveedor_principal_id
                WHERE p.estado = 'ACTIVO' AND p.stock_actual <= p.stock_minimo
                ORDER BY c.nombre, p.nombre
                """;
        return query(TipoReporteOperativo.PRODUCTOS_POR_COMPRAR,
                List.of("Producto", "Categoría", "Stock actual", "Stock mínimo", "Stock objetivo", "Cantidad sugerida", "Proveedor"), sql);
    }

    private ReporteOperativo bajoStock() {
        String sql = """
                SELECT p.nombre, c.nombre AS categoria, p.stock_actual, p.stock_minimo, COALESCE(p.stock_objetivo, '') AS stock_objetivo
                FROM producto p JOIN categoria c ON c.id = p.categoria_id
                WHERE p.estado = 'ACTIVO' AND p.stock_actual <= p.stock_minimo AND p.stock_actual > 0
                ORDER BY p.stock_actual ASC, p.nombre
                """;
        return query(TipoReporteOperativo.BAJO_STOCK, List.of("Producto", "Categoría", "Stock actual", "Stock mínimo", "Stock objetivo"), sql);
    }

    private ReporteOperativo agotados() {
        String sql = """
                SELECT p.nombre, c.nombre AS categoria, p.stock_actual, p.stock_minimo, COALESCE(pr.nombre, '') AS proveedor
                FROM producto p
                JOIN categoria c ON c.id = p.categoria_id
                LEFT JOIN proveedor pr ON pr.id = p.proveedor_principal_id
                WHERE p.estado = 'ACTIVO' AND p.stock_actual = 0
                ORDER BY c.nombre, p.nombre
                """;
        return query(TipoReporteOperativo.AGOTADOS, List.of("Producto", "Categoría", "Stock", "Stock mínimo", "Proveedor"), sql);
    }

    private ReporteOperativo proximosVencer() {
        String sql = """
                SELECT p.nombre, COALESCE(lp.codigo_lote, '') AS lote, lp.fecha_vencimiento, lp.cantidad_actual, COALESCE(pr.nombre, '') AS proveedor
                FROM lote_producto lp
                JOIN producto p ON p.id = lp.producto_id
                LEFT JOIN proveedor pr ON pr.id = lp.proveedor_id
                WHERE lp.fecha_vencimiento IS NOT NULL
                  AND lp.estado = 'DISPONIBLE'
                  AND date(lp.fecha_vencimiento) <= date('now', '+30 day')
                ORDER BY date(lp.fecha_vencimiento), p.nombre
                """;
        return query(TipoReporteOperativo.PROXIMOS_A_VENCER, List.of("Producto", "Lote", "Vence", "Cantidad", "Proveedor"), sql);
    }

    private ReporteOperativo inventarioValorizado() {
        String sql = """
                SELECT p.nombre, c.nombre AS categoria, p.stock_actual, p.precio_compra_referencia,
                       ROUND(p.stock_actual * p.precio_compra_referencia, 2) AS valor_compra,
                       ROUND(p.stock_actual * p.precio_venta, 2) AS valor_venta
                FROM producto p JOIN categoria c ON c.id = p.categoria_id
                WHERE p.estado = 'ACTIVO'
                ORDER BY c.nombre, p.nombre
                """;
        return query(TipoReporteOperativo.INVENTARIO_VALORIZADO, List.of("Producto", "Categoría", "Stock", "Precio compra", "Valor compra", "Valor venta"), sql);
    }

    private ReporteOperativo comprasRecientes() {
        String sql = """
                SELECT c.fecha_compra, COALESCE(p.nombre, '') AS proveedor, c.tipo_comprobante, COALESCE(c.numero_comprobante, '') AS comprobante, c.total_estimado
                FROM compra c LEFT JOIN proveedor p ON p.id = c.proveedor_id
                ORDER BY c.fecha_compra DESC, c.id DESC
                LIMIT 100
                """;
        return query(TipoReporteOperativo.COMPRAS_RECIENTES, List.of("Fecha", "Proveedor", "Tipo", "Comprobante", "Total"), sql);
    }

    private ReporteOperativo ventasRecientes() {
        String sql = """
                SELECT fecha_venta, metodo_pago, total, COALESCE(numero_referencia, '') AS referencia, estado
                FROM venta_interna
                ORDER BY fecha_venta DESC, id DESC
                LIMIT 100
                """;
        return query(TipoReporteOperativo.VENTAS_INTERNAS_RECIENTES, List.of("Fecha", "Método", "Total", "Referencia", "Estado"), sql);
    }

    private ReporteOperativo mermasRecientes() {
        String sql = """
                SELECT mr.fecha_retiro, p.nombre, mr.tipo_motivo, mr.cantidad, COALESCE(mr.observacion, '') AS observacion
                FROM merma_retiro mr JOIN producto p ON p.id = mr.producto_id
                ORDER BY mr.fecha_retiro DESC, mr.id DESC
                LIMIT 100
                """;
        return query(TipoReporteOperativo.MERMAS_RETIROS_RECIENTES, List.of("Fecha", "Producto", "Motivo", "Cantidad", "Observación"), sql);
    }


    private ReporteOperativo cierresCajaRecientes() {
        String sql = """
                SELECT fecha, saldo_inicial, total_ingresos, total_egresos, saldo_esperado,
                       COALESCE(saldo_contado, '') AS saldo_contado, diferencia, estado
                FROM caja_diaria
                ORDER BY date(fecha) DESC, id DESC
                LIMIT 90
                """;
        return query(TipoReporteOperativo.CIERRE_CAJA_RECIENTE,
                List.of("Fecha", "Saldo inicial", "Ingresos", "Egresos", "Saldo esperado", "Saldo contado", "Diferencia", "Estado"), sql);
    }

    private ReporteOperativo fiadoPendiente() {
        String sql = """
                SELECT cf.nombre, COALESCE(cf.telefono, '') AS telefono, cpc.fecha_apertura,
                       cpc.monto_original, cpc.saldo_pendiente, cpc.estado, COALESCE(cpc.observacion, '') AS observacion
                FROM cuenta_por_cobrar cpc
                JOIN cliente_fiado cf ON cf.id = cpc.cliente_fiado_id
                WHERE cpc.estado = 'ABIERTA' AND cpc.saldo_pendiente > 0
                ORDER BY cpc.saldo_pendiente DESC, cpc.fecha_apertura ASC
                """;
        return query(TipoReporteOperativo.FIADO_PENDIENTE,
                List.of("Cliente", "Teléfono", "Fecha", "Monto original", "Saldo", "Estado", "Observación"), sql);
    }

    private ReporteOperativo abonosRecientes() {
        String sql = """
                SELECT a.fecha_abono, cf.nombre, a.monto, a.metodo_pago,
                       COALESCE(a.observacion, '') AS observacion,
                       CASE WHEN a.movimiento_caja_id IS NULL THEN 'Sin caja' ELSE 'Con caja' END AS caja
                FROM abono a
                JOIN cuenta_por_cobrar cpc ON cpc.id = a.cuenta_por_cobrar_id
                JOIN cliente_fiado cf ON cf.id = cpc.cliente_fiado_id
                ORDER BY datetime(a.fecha_abono) DESC, a.id DESC
                LIMIT 100
                """;
        return query(TipoReporteOperativo.ABONOS_RECIENTES,
                List.of("Fecha", "Cliente", "Monto", "Método", "Observación", "Caja"), sql);
    }

    private ReporteOperativo gastosOperativos() {
        String sql = """
                SELECT go.fecha_gasto, tg.nombre, go.monto, go.forma_pago_codigo,
                       go.descripcion, COALESCE(go.referencia, '') AS referencia,
                       CASE WHEN go.movimiento_caja_id IS NULL THEN 'Sin caja' ELSE 'Con caja' END AS caja
                FROM gasto_operativo go
                JOIN tipo_gasto tg ON tg.id = go.tipo_gasto_id
                ORDER BY datetime(go.fecha_gasto) DESC, go.id DESC
                LIMIT 100
                """;
        return query(TipoReporteOperativo.GASTOS_OPERATIVOS,
                List.of("Fecha", "Tipo", "Monto", "Forma", "Descripción", "Referencia", "Caja"), sql);
    }

    private ReporteOperativo cuentasPorPagar() {
        String sql = """
                SELECT COALESCE(p.nombre, '') AS proveedor, c.fecha_compra,
                       COALESCE(c.numero_comprobante, '') AS comprobante,
                       cpp.monto_total, cpp.saldo_pendiente,
                       COALESCE(cpp.fecha_vencimiento, '') AS vence, cpp.estado
                FROM cuenta_por_pagar cpp
                JOIN compra c ON c.id = cpp.compra_id
                LEFT JOIN proveedor p ON p.id = cpp.proveedor_id
                WHERE cpp.estado IN ('PENDIENTE','PARCIAL') AND cpp.saldo_pendiente > 0
                ORDER BY date(COALESCE(cpp.fecha_vencimiento, c.fecha_compra)) ASC, cpp.saldo_pendiente DESC
                """;
        return query(TipoReporteOperativo.CUENTAS_POR_PAGAR,
                List.of("Proveedor", "Fecha compra", "Comprobante", "Total", "Saldo", "Vence", "Estado"), sql);
    }

    private ReporteOperativo query(TipoReporteOperativo tipo, List<String> headers, String sql) {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<ReporteLinea> lineas = new ArrayList<>();
            int cols = headers.size();
            while (rs.next()) {
                List<String> values = new ArrayList<>();
                for (int i = 1; i <= cols; i++) values.add(value(rs, i));
                lineas.add(new ReporteLinea(values));
            }
            return new ReporteOperativo(tipo, headers, lineas, LocalDateTime.now());
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo generar el reporte: " + tipo.label(), ex);
        }
    }

    private String value(ResultSet rs, int index) throws SQLException {
        Object value = rs.getObject(index);
        if (value == null) return "";
        if (value instanceof BigDecimal bd) return bd.stripTrailingZeros().toPlainString();
        return String.valueOf(value);
    }
}
