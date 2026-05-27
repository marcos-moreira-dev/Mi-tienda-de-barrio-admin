package com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta;

/** Comando para anular una venta interna de forma controlada. */
public record RegistroAnulacionVentaInterna(
        Long ventaInternaId,
        String motivo,
        String responsableTexto
) {}
