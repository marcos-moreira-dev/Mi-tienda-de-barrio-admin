package com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto;

import java.math.BigDecimal;

/** Producto comercial con stock local y metadatos suficientes para tienda/despensa. */
public record Producto(
        Long id,
        String codigoInterno,
        String nombre,
        String descripcion,
        Long categoriaId,
        String categoriaNombre,
        Long marcaId,
        String marcaNombre,
        Long unidadMedidaId,
        String unidadMedidaNombre,
        Long proveedorPrincipalId,
        String proveedorPrincipalNombre,
        String presentacion,
        BigDecimal precioCompraReferencia,
        BigDecimal precioVenta,
        BigDecimal stockActual,
        BigDecimal stockMinimo,
        BigDecimal stockObjetivo,
        boolean manejaLote,
        boolean manejaVencimiento,
        boolean perecible,
        boolean refrigerado,
        String rutaFoto,
        EstadoProducto estado,
        String observacion
) {
    public boolean bajoStock() {
        return stockActual != null && stockMinimo != null && stockActual.compareTo(stockMinimo) <= 0;
    }

    public BigDecimal cantidadSugeridaCompra() {
        BigDecimal objetivo = stockObjetivo != null ? stockObjetivo : stockMinimo;
        if (objetivo == null || stockActual == null || objetivo.compareTo(stockActual) <= 0) {
            return BigDecimal.ZERO;
        }
        return objetivo.subtract(stockActual);
    }
}
