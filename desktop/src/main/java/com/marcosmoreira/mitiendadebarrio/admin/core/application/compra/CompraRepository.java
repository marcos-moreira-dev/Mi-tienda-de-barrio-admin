package com.marcosmoreira.mitiendadebarrio.admin.core.application.compra;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.Compra;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.CuentaPorPagar;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.RegistroCompraAvanzada;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.RegistroCompraSimple;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.RegistroPagoProveedor;

import java.util.List;

/** Puerto de persistencia para compras/entradas y cuentas por pagar locales. */
public interface CompraRepository {
    List<Compra> findRecent(String query, int limit);
    Compra registrarCompraSimple(RegistroCompraSimple command);
    Compra registrarCompraAvanzada(RegistroCompraAvanzada command);
    CuentaPorPagar registrarPagoProveedor(RegistroPagoProveedor command);
    List<CuentaPorPagar> listarCuentasPorPagarPendientes(int limit);
}
