package com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad;

import java.math.BigDecimal;
import java.util.List;

/** Documento local/preparado. No reemplaza comprobante autorizado por el SRI. */
public record DocumentoFiscalPreparado(
        Long id,
        String tipoComprobanteCodigo,
        Long terceroId,
        Long ventaInternaId,
        Long compraId,
        String secuencia,
        String fechaEmision,
        EstadoDocumentoFiscalPreparado estado,
        BigDecimal subtotal,
        BigDecimal impuestoTotal,
        BigDecimal total,
        String advertenciaNoAutorizado,
        String observacion,
        List<DocumentoFiscalPreparadoDetalle> detalles
) {}
