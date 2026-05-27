package com.marcosmoreira.mitiendadebarrio.admin.core.application.cartera;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.cartera.CarteraCajaResultado;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.cartera.RegistroAbonoConCaja;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.cartera.RegistroPagoProveedorConCaja;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.cartera.RegistroVentaPagadaEnCaja;

/** Puerto para operaciones integradas entre cartera, ventas, pagos y caja local. */
public interface CarteraLocalRepository {
    CarteraCajaResultado registrarAbonoConCaja(RegistroAbonoConCaja command);
    CarteraCajaResultado registrarPagoProveedorConCaja(RegistroPagoProveedorConCaja command);
    CarteraCajaResultado registrarVentaPagadaEnCaja(RegistroVentaPagadaEnCaja command);
}
