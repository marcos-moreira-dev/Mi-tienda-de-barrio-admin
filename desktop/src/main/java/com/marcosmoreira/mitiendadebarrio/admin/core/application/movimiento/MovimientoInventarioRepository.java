package com.marcosmoreira.mitiendadebarrio.admin.core.application.movimiento;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.movimiento.MovimientoInventario;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.movimiento.TipoMovimientoInventario;

import java.math.BigDecimal;
import java.util.List;

/** Puerto de persistencia para movimientos de inventario. */
public interface MovimientoInventarioRepository {
    List<MovimientoInventario> findRecent(String query, int limit);
    MovimientoInventario registrarAjuste(Long productoId, TipoMovimientoInventario tipo, BigDecimal cantidad, String motivo, String responsable, String observacion);
}
