package com.marcosmoreira.mitiendadebarrio.admin.core.application.catalogo;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.EstadoCatalogo;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.Marca;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.util.List;

/** Casos de uso de marcas. */
public final class MarcaService {
    private final MarcaRepository repository;
    private final WriteAccessGuard writeAccessGuard;

    public MarcaService(MarcaRepository repository) {
        this(repository, null);
    }

    public MarcaService(MarcaRepository repository, WriteAccessGuard writeAccessGuard) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
    }

    public List<Marca> listar(boolean incluirInactivas) {
        return repository.findAll(incluirInactivas);
    }

    public OperationResult<Marca> guardar(Marca marca) {
        OperationResult<Marca> blocked = bloquearSiNoPuedeEscribir("guardar marca");
        if (blocked != null) return blocked;
        try {
            validar(marca);
            Marca guardada = repository.save(marca);
            return OperationResult.success(guardada, "Marca guardada correctamente.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo guardar la marca. Revise si el nombre ya existe.");
        }
    }

    public OperationResult<Void> desactivar(long id) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("desactivar marca");
        if (blocked != null) return blocked;
        repository.updateEstado(id, EstadoCatalogo.INACTIVA);
        return OperationResult.success(null, "Marca desactivada.");
    }

    public OperationResult<Void> reactivar(long id) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("reactivar marca");
        if (blocked != null) return blocked;
        repository.updateEstado(id, EstadoCatalogo.ACTIVA);
        return OperationResult.success(null, "Marca reactivada.");
    }

    private void validar(Marca marca) {
        if (marca.nombre() == null || marca.nombre().isBlank()) {
            throw new ValidationException("El nombre de la marca es obligatorio.");
        }
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }
}
