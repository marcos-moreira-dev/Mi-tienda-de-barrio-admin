package com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad;

import java.util.List;

/** Solicitud para crear un documento preparado local, sin emisión electrónica. */
public record CrearDocumentoFiscalPreparadoSolicitud(
        String tipoComprobanteCodigo,
        Long terceroId,
        Long ventaInternaId,
        Long compraId,
        String secuencia,
        String observacion,
        List<DocumentoFiscalPreparadoDetalle> detalles
) {}
