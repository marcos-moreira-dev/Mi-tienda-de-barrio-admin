package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.productos;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.Categoria;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.Marca;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.UnidadMedida;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.EstadoProducto;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.Producto;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.proveedor.Proveedor;

import java.math.BigDecimal;

/** Datos capturados desde el formulario de producto. */
public record ProductoFormData(
        String codigoInterno,
        String nombre,
        String descripcion,
        Categoria categoria,
        Marca marca,
        UnidadMedida unidadMedida,
        Proveedor proveedorPrincipal,
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
        String observacion
) {
    public Producto toProducto(Producto seleccionado) {
        return new Producto(
                seleccionado == null ? null : seleccionado.id(),
                codigoInterno,
                nombre,
                descripcion,
                categoria == null ? null : categoria.id(),
                categoria == null ? "" : categoria.nombre(),
                marca == null ? null : marca.id(),
                marca == null ? "" : marca.nombre(),
                unidadMedida == null ? null : unidadMedida.id(),
                unidadMedida == null ? "" : unidadMedida.nombre(),
                proveedorPrincipal == null ? null : proveedorPrincipal.id(),
                proveedorPrincipal == null ? "" : proveedorPrincipal.nombre(),
                presentacion,
                zeroIfNull(precioCompraReferencia),
                zeroIfNull(precioVenta),
                zeroIfNull(stockActual),
                zeroIfNull(stockMinimo),
                stockObjetivo,
                manejaLote,
                manejaVencimiento,
                perecible,
                refrigerado,
                rutaFoto,
                seleccionado == null ? EstadoProducto.ACTIVO : seleccionado.estado(),
                observacion
        );
    }

    private static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
