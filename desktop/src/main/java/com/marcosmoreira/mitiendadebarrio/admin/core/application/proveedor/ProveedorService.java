package com.marcosmoreira.mitiendadebarrio.admin.core.application.proveedor;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.proveedor.EstadoProveedor;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.proveedor.Proveedor;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.util.List;

/** Casos de uso locales de proveedores. */
public final class ProveedorService {
    private final ProveedorRepository repository;
    private final WriteAccessGuard writeAccessGuard;

    public ProveedorService(ProveedorRepository repository) {
        this(repository, null);
    }

    public ProveedorService(ProveedorRepository repository, WriteAccessGuard writeAccessGuard) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
    }

    public List<Proveedor> listar(boolean incluirInactivos) {
        return repository.findAll(incluirInactivos);
    }

    public OperationResult<Proveedor> guardar(Proveedor proveedor) {
        OperationResult<Proveedor> blocked = bloquearSiNoPuedeEscribir("guardar proveedor");
        if (blocked != null) return blocked;
        try {
            validar(proveedor);
            Proveedor guardado = repository.save(proveedor);
            return OperationResult.success(guardado, "Proveedor guardado correctamente.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo guardar el proveedor. Revise si ya existe uno con el mismo nombre y teléfono.");
        }
    }

    public OperationResult<Void> desactivar(long id) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("desactivar proveedor");
        if (blocked != null) return blocked;
        repository.updateEstado(id, EstadoProveedor.INACTIVO);
        return OperationResult.success(null, "Proveedor desactivado.");
    }

    public OperationResult<Void> reactivar(long id) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("reactivar proveedor");
        if (blocked != null) return blocked;
        repository.updateEstado(id, EstadoProveedor.ACTIVO);
        return OperationResult.success(null, "Proveedor reactivado.");
    }

    private void validar(Proveedor proveedor) {
        if (proveedor.nombre() == null || proveedor.nombre().isBlank()) {
            throw new ValidationException("El nombre del proveedor es obligatorio.");
        }
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }
}
