package com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Deuda local con proveedor generada por una compra a crédito. */
public record CuentaPorPagar(
        Long id,
        Long compraId,
        Long proveedorId,
        String proveedorNombre,
        LocalDate fechaEmision,
        LocalDate fechaVencimiento,
        BigDecimal montoTotal,
        BigDecimal saldoPendiente,
        String estado,
        String observacion
) {}
