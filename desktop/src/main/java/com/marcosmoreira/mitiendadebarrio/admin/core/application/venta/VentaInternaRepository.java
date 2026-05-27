package com.marcosmoreira.mitiendadebarrio.admin.core.application.venta;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.AnulacionVentaInterna;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.RegistroAnulacionVentaInterna;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.RegistroVentaInternaAvanzada;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.RegistroVentaInternaSimple;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.VentaInterna;

import java.util.List;

/** Puerto de persistencia para ventas internas y salidas de stock. */
public interface VentaInternaRepository {
    List<VentaInterna> findRecent(String query, int limit);
    VentaInterna registrarVentaSimple(RegistroVentaInternaSimple command);
    VentaInterna registrarVentaAvanzada(RegistroVentaInternaAvanzada command);
    AnulacionVentaInterna anularVenta(RegistroAnulacionVentaInterna command);
}
