package com.marcosmoreira.mitiendadebarrio.admin.core.application.catalogo;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.Categoria;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.EstadoCatalogo;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.util.List;

/** Casos de uso de categorías. */
public final class CategoriaService {
    private final CategoriaRepository repository;
    private final WriteAccessGuard writeAccessGuard;

    public CategoriaService(CategoriaRepository repository) {
        this(repository, null);
    }

    public CategoriaService(CategoriaRepository repository, WriteAccessGuard writeAccessGuard) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
    }

    public List<Categoria> listar(boolean incluirInactivas) {
        return repository.findAll(incluirInactivas);
    }

    public OperationResult<Categoria> guardar(Categoria categoria) {
        OperationResult<Categoria> blocked = bloquearSiNoPuedeEscribir("guardar categoría");
        if (blocked != null) return blocked;
        try {
            validar(categoria);
            Categoria guardada = repository.save(categoria);
            return OperationResult.success(guardada, "Categoría guardada correctamente.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo guardar la categoría. Revise si el nombre ya existe.");
        }
    }

    public OperationResult<Void> desactivar(long id) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("desactivar categoría");
        if (blocked != null) return blocked;
        repository.updateEstado(id, EstadoCatalogo.INACTIVA);
        return OperationResult.success(null, "Categoría desactivada.");
    }

    public OperationResult<Void> reactivar(long id) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("reactivar categoría");
        if (blocked != null) return blocked;
        repository.updateEstado(id, EstadoCatalogo.ACTIVA);
        return OperationResult.success(null, "Categoría reactivada.");
    }

    private void validar(Categoria categoria) {
        if (categoria.nombre() == null || categoria.nombre().isBlank()) {
            throw new ValidationException("El nombre de la categoría es obligatorio.");
        }
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }
}
