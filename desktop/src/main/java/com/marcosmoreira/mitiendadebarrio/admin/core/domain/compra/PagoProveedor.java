package com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Pago local aplicado a una cuenta por pagar de proveedor. */
public record PagoProveedor(
        Long id,
        Long cuentaPorPagarId,
        LocalDate fechaPago,
        BigDecimal monto,
        String formaPago,
        String referencia,
        String observacion
) {}
