package com.marcosmoreira.mitiendadebarrio.admin.core.application.inventario;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.inventario.*;

import java.math.BigDecimal;
import java.util.List;

/** Puerto de persistencia para conteos y ajustes formales de inventario local. */
public interface InventarioFuerteRepository {
    List<TipoMovimientoInventarioCatalogo> listarTiposMovimiento();
    ConteoInventario crearConteo(String responsableTexto, String observacion);
    ConteoInventarioDetalle registrarDetalleConteo(Long conteoId, Long productoId, BigDecimal stockContado, String observacion);
    List<ConteoInventarioDetalle> listarDetallesConteo(Long conteoId);
    ConteoInventario cerrarConteo(Long conteoId);
    AjusteInventarioResultado registrarAjuste(AjusteInventarioSolicitud solicitud);
}
