package com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta;

import java.util.List;

/** Comando para registrar una venta interna con varios productos. */
public record RegistroVentaInternaAvanzada(
        Long clienteFiadoId,
        List<DetalleVentaInternaAvanzada> detalles,
        MetodoPagoVentaInterna metodoPago,
        String numeroReferencia,
        boolean advertenciaTributariaAceptada,
        String observacion
) {
    public boolean esFiada() {
        return metodoPago == MetodoPagoVentaInterna.FIADO;
    }
}
