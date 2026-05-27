package com.marcosmoreira.mitiendadebarrio.admin.core.application.catalogo;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.EstadoCatalogo;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.UnidadMedida;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.util.List;

/** Casos de uso de unidades de medida. */
public final class UnidadMedidaService {
    private final UnidadMedidaRepository repository;
    private final WriteAccessGuard writeAccessGuard;

    public UnidadMedidaService(UnidadMedidaRepository repository) {
        this(repository, null);
    }

    public UnidadMedidaService(UnidadMedidaRepository repository, WriteAccessGuard writeAccessGuard) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
    }

    public List<UnidadMedida> listar(boolean incluirInactivas) {
        return repository.findAll(incluirInactivas);
    }

    public OperationResult<UnidadMedida> guardar(UnidadMedida unidadMedida) {
        OperationResult<UnidadMedida> blocked = bloquearSiNoPuedeEscribir("guardar unidad de medida");
        if (blocked != null) return blocked;
        try {
            validar(unidadMedida);
            UnidadMedida guardada = repository.save(unidadMedida);
            return OperationResult.success(guardada, "Unidad de medida guardada correctamente.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo guardar la unidad de medida. Revise si el nombre ya existe.");
        }
    }

    public OperationResult<Void> desactivar(long id) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("desactivar unidad de medida");
        if (blocked != null) return blocked;
        repository.updateEstado(id, EstadoCatalogo.INACTIVA);
        return OperationResult.success(null, "Unidad de medida desactivada.");
    }

    public OperationResult<Void> reactivar(long id) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("reactivar unidad de medida");
        if (blocked != null) return blocked;
        repository.updateEstado(id, EstadoCatalogo.ACTIVA);
        return OperationResult.success(null, "Unidad de medida reactivada.");
    }

    private void validar(UnidadMedida unidadMedida) {
        if (unidadMedida.nombre() == null || unidadMedida.nombre().isBlank()) {
            throw new ValidationException("El nombre de la unidad de medida es obligatorio.");
        }
        if (unidadMedida.abreviatura() == null || unidadMedida.abreviatura().isBlank()) {
            throw new ValidationException("La abreviatura de la unidad de medida es obligatoria.");
        }
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }
}
