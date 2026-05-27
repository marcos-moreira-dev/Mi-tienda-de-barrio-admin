package com.marcosmoreira.mitiendadebarrio.admin.core.application.fiscalidad;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.CrearDocumentoFiscalPreparadoSolicitud;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.DocumentoFiscalPreparado;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.ImpuestoConfiguracion;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.TipoComprobanteLocal;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.TipoIdentificacionLocal;

import java.util.List;
import java.util.Optional;

/** Puerto local para fiscalidad preparada, no-SRI. */
public interface FiscalidadPreparadaRepository {
    List<TipoIdentificacionLocal> listarTiposIdentificacionActivos();
    List<TipoComprobanteLocal> listarTiposComprobanteActivos();
    List<ImpuestoConfiguracion> listarImpuestosActivos();
    Optional<DocumentoFiscalPreparado> buscarDocumentoPorId(long id);
    DocumentoFiscalPreparado crearDocumentoPreparado(CrearDocumentoFiscalPreparadoSolicitud solicitud);
    void anularDocumento(long documentoId, String motivo);
}
