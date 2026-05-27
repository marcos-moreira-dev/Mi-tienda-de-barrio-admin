package com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Compra o recepción de mercadería registrada localmente. */
public record Compra(
        Long id,
        Long proveedorId,
        String proveedorNombre,
        LocalDate fechaCompra,
        String numeroComprobante,
        TipoComprobanteCompra tipoComprobante,
        BigDecimal totalEstimado,
        String estado,
        String observacion
) {}
