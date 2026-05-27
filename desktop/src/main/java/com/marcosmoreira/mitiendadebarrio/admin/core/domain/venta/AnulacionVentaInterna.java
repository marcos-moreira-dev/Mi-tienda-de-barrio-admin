package com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta;

import java.time.LocalDateTime;

/** Evidencia de anulación controlada de una venta interna. */
public record AnulacionVentaInterna(
        Long id,
        Long ventaInternaId,
        LocalDateTime fechaAnulacion,
        String motivo,
        String responsableTexto
) {}
