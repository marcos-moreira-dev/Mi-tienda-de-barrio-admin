package com.marcosmoreira.mitiendadebarrio.admin.core.application.proveedor;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.proveedor.EstadoProveedor;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.proveedor.Proveedor;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia para proveedores. */
public interface ProveedorRepository {
    List<Proveedor> findAll(boolean includeInactive);
    Optional<Proveedor> findById(long id);
    Proveedor save(Proveedor proveedor);
    void updateEstado(long id, EstadoProveedor estado);
}
