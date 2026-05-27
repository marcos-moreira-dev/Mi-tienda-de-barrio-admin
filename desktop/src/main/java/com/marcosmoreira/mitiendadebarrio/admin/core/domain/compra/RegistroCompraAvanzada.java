package com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra;

import java.time.LocalDate;
import java.util.List;

/** Comando para registrar una compra con varios detalles y pago contado/crédito. */
public record RegistroCompraAvanzada(
        Long proveedorId,
        LocalDate fechaCompra,
        TipoComprobanteCompra tipoComprobante,
        String numeroComprobante,
        boolean compraCredito,
        LocalDate fechaVencimientoPago,
        String observacion,
        List<DetalleCompraAvanzada> detalles
) {}
