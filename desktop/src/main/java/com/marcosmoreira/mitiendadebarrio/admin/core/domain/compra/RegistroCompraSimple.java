package com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Comando simple para registrar una compra de un producto. */
public record RegistroCompraSimple(
        Long proveedorId,
        Long productoId,
        BigDecimal cantidad,
        BigDecimal costoUnitario,
        LocalDate fechaCompra,
        TipoComprobanteCompra tipoComprobante,
        String numeroComprobante,
        String codigoLote,
        LocalDate fechaVencimiento,
        String observacion
) {}
