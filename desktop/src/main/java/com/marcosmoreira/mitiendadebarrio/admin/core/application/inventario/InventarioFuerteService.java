package com.marcosmoreira.mitiendadebarrio.admin.core.application.inventario;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.auditoria.AuditoriaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.inventario.*;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.movimiento.TipoMovimientoInventario;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.math.BigDecimal;
import java.util.List;

/** Casos de uso para inventario fuerte: conteos físicos y ajustes formales. */
public final class InventarioFuerteService {
    private final InventarioFuerteRepository repository;
    private final WriteAccessGuard writeAccessGuard;
    private final AuditoriaService auditoriaService;

    public InventarioFuerteService(InventarioFuerteRepository repository, WriteAccessGuard writeAccessGuard, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
        this.auditoriaService = auditoriaService;
    }

    public List<TipoMovimientoInventarioCatalogo> tiposMovimiento() {
        return repository.listarTiposMovimiento();
    }

    public OperationResult<ConteoInventario> crearConteo(String responsableTexto, String observacion) {
        OperationResult<ConteoInventario> blocked = bloquearSiNoPuedeEscribir("crear conteo de inventario");
        if (blocked != null) return blocked;
        try {
            ConteoInventario conteo = repository.crearConteo(responsableTexto, observacion);
            auditar("CONTEO_INVENTARIO_CREADO", "conteo_inventario", conteo.id(), "Conteo de inventario creado.");
            return OperationResult.success(conteo, "Conteo de inventario creado.");
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo crear el conteo de inventario.");
        }
    }

    public OperationResult<ConteoInventarioDetalle> registrarDetalleConteo(Long conteoId, Long productoId, BigDecimal stockContado, String observacion) {
        OperationResult<ConteoInventarioDetalle> blocked = bloquearSiNoPuedeEscribir("registrar línea de conteo de inventario");
        if (blocked != null) return blocked;
        try {
            if (conteoId == null) throw new ValidationException("Debe seleccionar un conteo.");
            if (productoId == null) throw new ValidationException("Debe seleccionar un producto.");
            if (stockContado == null || stockContado.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("La cantidad contada no puede ser negativa.");
            }
            ConteoInventarioDetalle detalle = repository.registrarDetalleConteo(conteoId, productoId, stockContado, observacion);
            auditar("CONTEO_INVENTARIO_DETALLE", "conteo_inventario", conteoId, "Línea de conteo registrada para producto " + productoId + ".");
            return OperationResult.success(detalle, "Línea de conteo registrada.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar la línea de conteo.");
        }
    }

    public List<ConteoInventarioDetalle> detallesConteo(Long conteoId) {
        return repository.listarDetallesConteo(conteoId);
    }

    public OperationResult<ConteoInventario> cerrarConteo(Long conteoId) {
        OperationResult<ConteoInventario> blocked = bloquearSiNoPuedeEscribir("cerrar conteo de inventario");
        if (blocked != null) return blocked;
        try {
            if (conteoId == null) throw new ValidationException("Debe seleccionar un conteo.");
            ConteoInventario conteo = repository.cerrarConteo(conteoId);
            auditar("CONTEO_INVENTARIO_CERRADO", "conteo_inventario", conteo.id(), "Conteo de inventario cerrado.");
            return OperationResult.success(conteo, "Conteo cerrado.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo cerrar el conteo.");
        }
    }

    public OperationResult<AjusteInventarioResultado> registrarAjuste(AjusteInventarioSolicitud solicitud) {
        OperationResult<AjusteInventarioResultado> blocked = bloquearSiNoPuedeEscribir("registrar ajuste formal de inventario");
        if (blocked != null) return blocked;
        try {
            validarAjuste(solicitud);
            AjusteInventarioResultado resultado = repository.registrarAjuste(solicitud);
            auditar("AJUSTE_INVENTARIO_REGISTRADO", "ajuste_inventario", resultado.ajusteInventarioId(), "Ajuste formal de inventario registrado.");
            return OperationResult.success(resultado, "Ajuste de inventario registrado y stock actualizado.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar el ajuste de inventario. Revise stock, producto y motivo.");
        }
    }

    private void validarAjuste(AjusteInventarioSolicitud solicitud) {
        if (solicitud == null) throw new ValidationException("Debe indicar los datos del ajuste.");
        if (solicitud.productoId() == null) throw new ValidationException("Debe seleccionar un producto.");
        if (solicitud.tipoMovimiento() == null) throw new ValidationException("Debe seleccionar el tipo de ajuste.");
        if (solicitud.tipoMovimiento() == TipoMovimientoInventario.ENTRADA_COMPRA || solicitud.tipoMovimiento() == TipoMovimientoInventario.SALIDA_VENTA_INTERNA) {
            throw new ValidationException("Compras y ventas tienen su propio flujo. Use ajustes, mermas o correcciones para este módulo.");
        }
        if (solicitud.cantidad() == null || solicitud.cantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("La cantidad debe ser mayor que cero.");
        }
        if (solicitud.motivo() == null || solicitud.motivo().isBlank()) {
            throw new ValidationException("Debe escribir un motivo para trazabilidad.");
        }
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }

    private void auditar(String accion, String entidad, Long entidadId, String resumen) {
        if (auditoriaService == null) return;
        try {
            auditoriaService.registrarExito(null, "Inventario", accion, entidad, entidadId, resumen);
        } catch (RuntimeException ignored) {
            // La auditoría no debe tumbar la operación principal en esta etapa local.
        }
    }
}
