package com.marcosmoreira.mitiendadebarrio.admin.core.domain.reporte;

/** Tipos de reporte operativo disponibles en la app local. */
public enum TipoReporteOperativo {
    PRODUCTOS_POR_COMPRAR("Productos por comprar"),
    BAJO_STOCK("Bajo stock"),
    AGOTADOS("Agotados"),
    PROXIMOS_A_VENCER("Próximos a vencer"),
    INVENTARIO_VALORIZADO("Inventario valorizado"),
    COMPRAS_RECIENTES("Compras recientes"),
    VENTAS_INTERNAS_RECIENTES("Ventas internas recientes"),
    MERMAS_RETIROS_RECIENTES("Mermas y retiros recientes"),
    CIERRE_CAJA_RECIENTE("Cierres de caja recientes"),
    FIADO_PENDIENTE("Fiado pendiente"),
    ABONOS_RECIENTES("Abonos recientes"),
    GASTOS_OPERATIVOS("Gastos operativos"),
    CUENTAS_POR_PAGAR("Cuentas por pagar");

    private final String label;

    TipoReporteOperativo(String label) { this.label = label; }

    public String label() { return label; }
}
