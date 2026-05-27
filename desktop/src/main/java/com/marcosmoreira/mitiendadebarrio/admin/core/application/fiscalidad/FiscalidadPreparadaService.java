package com.marcosmoreira.mitiendadebarrio.admin.core.application.fiscalidad;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.auditoria.AuditoriaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.CrearDocumentoFiscalPreparadoSolicitud;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.DocumentoFiscalPreparado;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.DocumentoFiscalPreparadoDetalle;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.ImpuestoConfiguracion;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.TipoComprobanteLocal;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad.TipoIdentificacionLocal;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Casos de uso de fiscalidad preparada local. No emite ni autoriza documentos del SRI. */
public final class FiscalidadPreparadaService {
    private static final String MODULO = "Fiscalidad preparada";

    private final FiscalidadPreparadaRepository repository;
    private final WriteAccessGuard writeAccessGuard;
    private final AuditoriaService auditoriaService;

    public FiscalidadPreparadaService(FiscalidadPreparadaRepository repository) {
        this(repository, null, null);
    }

    public FiscalidadPreparadaService(
            FiscalidadPreparadaRepository repository,
            WriteAccessGuard writeAccessGuard,
            AuditoriaService auditoriaService
    ) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
        this.auditoriaService = auditoriaService;
    }

    public List<TipoIdentificacionLocal> listarTiposIdentificacionActivos() {
        return repository.listarTiposIdentificacionActivos();
    }

    public List<TipoComprobanteLocal> listarTiposComprobanteActivos() {
        return repository.listarTiposComprobanteActivos();
    }

    public List<ImpuestoConfiguracion> listarImpuestosActivos() {
        return repository.listarImpuestosActivos();
    }

    public Optional<DocumentoFiscalPreparado> buscarDocumentoPorId(long id) {
        return repository.buscarDocumentoPorId(id);
    }

    public OperationResult<DocumentoFiscalPreparado> crearDocumentoPreparado(CrearDocumentoFiscalPreparadoSolicitud solicitud) {
        OperationResult<DocumentoFiscalPreparado> blocked = bloquearSiNoPuedeEscribir("crear documento preparado");
        if (blocked != null) return blocked;
        try {
            validarSolicitud(solicitud);
            DocumentoFiscalPreparado documento = repository.crearDocumentoPreparado(solicitud);
            auditar("CREAR_DOCUMENTO_PREPARADO", documento.id(), "Documento preparado creado: " + documento.secuencia());
            return OperationResult.success(documento, "Documento preparado creado. Recuerde que no reemplaza comprobante autorizado por el SRI.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo crear el documento preparado. Revise tipo, detalles e importes.");
        }
    }

    public OperationResult<Void> anularDocumento(long documentoId, String motivo) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("anular documento preparado");
        if (blocked != null) return blocked;
        if (documentoId <= 0) {
            return OperationResult.failure("Debe indicar el documento preparado a anular.");
        }
        if (motivo == null || motivo.isBlank()) {
            return OperationResult.failure("Debe indicar un motivo para anular el documento preparado.");
        }
        repository.anularDocumento(documentoId, motivo.strip());
        auditar("ANULAR_DOCUMENTO_PREPARADO", documentoId, "Documento preparado anulado. Motivo: " + motivo.strip());
        return OperationResult.success(null, "Documento preparado anulado.");
    }

    private void validarSolicitud(CrearDocumentoFiscalPreparadoSolicitud solicitud) {
        if (solicitud == null) {
            throw new ValidationException("Debe indicar los datos del documento preparado.");
        }
        if (solicitud.tipoComprobanteCodigo() == null || solicitud.tipoComprobanteCodigo().isBlank()) {
            throw new ValidationException("Debe indicar el tipo de documento preparado.");
        }
        if (solicitud.ventaInternaId() == null && solicitud.compraId() == null && solicitud.terceroId() == null) {
            throw new ValidationException("El documento preparado debe relacionarse con venta, compra o cliente/proveedor.");
        }
        if (solicitud.detalles() == null || solicitud.detalles().isEmpty()) {
            throw new ValidationException("El documento preparado debe tener al menos un detalle.");
        }
        for (DocumentoFiscalPreparadoDetalle detalle : solicitud.detalles()) {
            if (detalle.descripcion() == null || detalle.descripcion().isBlank()) {
                throw new ValidationException("Cada detalle debe tener descripción.");
            }
            if (detalle.cantidad() == null || detalle.cantidad().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Cada detalle debe tener cantidad mayor a cero.");
            }
            if (detalle.precioUnitario() == null || detalle.precioUnitario().compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("Cada detalle debe tener precio unitario válido.");
            }
        }
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }

    private void auditar(String accion, Long entidadId, String resumen) {
        if (auditoriaService == null) return;
        auditoriaService.registrarExito(null, MODULO, accion, "documento_fiscal_preparado", entidadId, resumen);
    }
}
