package com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Comando para registrar un pago a proveedor sobre una cuenta por pagar. */
public record RegistroPagoProveedor(
        Long cuentaPorPagarId,
        BigDecimal monto,
        LocalDate fechaPago,
        String formaPago,
        String referencia,
        String observacion
) {}
