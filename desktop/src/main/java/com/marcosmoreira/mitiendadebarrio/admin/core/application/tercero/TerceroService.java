package com.marcosmoreira.mitiendadebarrio.admin.core.application.tercero;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.auditoria.AuditoriaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.tercero.EstadoTercero;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.tercero.Tercero;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.util.List;
import java.util.Optional;

/** Casos de uso locales de clientes/proveedores unificados como terceros. */
public final class TerceroService {
    private final TerceroRepository repository;
    private final WriteAccessGuard writeAccessGuard;
    private final AuditoriaService auditoriaService;

    public TerceroService(TerceroRepository repository) {
        this(repository, null, null);
    }

    public TerceroService(TerceroRepository repository, WriteAccessGuard writeAccessGuard, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
        this.auditoriaService = auditoriaService;
    }

    public List<Tercero> listar(boolean incluirInactivos) {
        return repository.findAll(incluirInactivos);
    }

    public Optional<Tercero> buscarPorId(long id) {
        return repository.findById(id);
    }

    public OperationResult<Tercero> guardar(Tercero tercero) {
        OperationResult<Tercero> blocked = bloquearSiNoPuedeEscribir("guardar cliente/proveedor");
        if (blocked != null) return blocked;
        try {
            validar(tercero);
            Tercero guardado = repository.save(tercero);
            auditar("GUARDAR_TERCERO", guardado.id(), "Cliente/proveedor guardado: " + guardado.nombreVisible());
            return OperationResult.success(guardado, "Cliente/proveedor guardado correctamente.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo guardar el cliente/proveedor. Revise identificación, nombre o datos repetidos.");
        }
    }

    public OperationResult<Void> marcarComoCliente(long terceroId, boolean permiteFiado) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("marcar cliente");
        if (blocked != null) return blocked;
        repository.asegurarCliente(terceroId, permiteFiado);
        auditar("MARCAR_CLIENTE", terceroId, "Tercero marcado como cliente.");
        return OperationResult.success(null, "Cliente activado para este registro.");
    }

    public OperationResult<Void> marcarComoProveedor(long terceroId) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("marcar proveedor");
        if (blocked != null) return blocked;
        repository.asegurarProveedor(terceroId);
        auditar("MARCAR_PROVEEDOR", terceroId, "Tercero marcado como proveedor.");
        return OperationResult.success(null, "Proveedor activado para este registro.");
    }

    public OperationResult<Void> desactivar(long id) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("desactivar cliente/proveedor");
        if (blocked != null) return blocked;
        repository.updateEstado(id, EstadoTercero.INACTIVO);
        auditar("DESACTIVAR_TERCERO", id, "Cliente/proveedor desactivado.");
        return OperationResult.success(null, "Cliente/proveedor desactivado.");
    }

    public OperationResult<Void> reactivar(long id) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("reactivar cliente/proveedor");
        if (blocked != null) return blocked;
        repository.updateEstado(id, EstadoTercero.ACTIVO);
        auditar("REACTIVAR_TERCERO", id, "Cliente/proveedor reactivado.");
        return OperationResult.success(null, "Cliente/proveedor reactivado.");
    }

    private void validar(Tercero tercero) {
        if (tercero == null) {
            throw new ValidationException("Debe indicar los datos del cliente/proveedor.");
        }
        boolean sinNombreLegal = tercero.nombreLegal() == null || tercero.nombreLegal().isBlank();
        boolean sinNombreComercial = tercero.nombreComercial() == null || tercero.nombreComercial().isBlank();
        if (sinNombreLegal && sinNombreComercial) {
            throw new ValidationException("Debe indicar nombre, razón social o nombre comercial.");
        }
        if (tercero.correo() != null && !tercero.correo().isBlank() && !tercero.correo().contains("@")) {
            throw new ValidationException("El correo del cliente/proveedor no parece válido.");
        }
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }

    private void auditar(String accion, Long entidadId, String resumen) {
        if (auditoriaService == null) {
            return;
        }
        auditoriaService.registrarExito(null, "Terceros", accion, "tercero", entidadId, resumen);
    }
}
