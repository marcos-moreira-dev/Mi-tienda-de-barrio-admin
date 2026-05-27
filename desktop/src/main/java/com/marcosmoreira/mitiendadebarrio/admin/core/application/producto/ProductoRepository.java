package com.marcosmoreira.mitiendadebarrio.admin.core.application.producto;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.EstadoProducto;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.Producto;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia para productos. */
public interface ProductoRepository {
    List<Producto> findAll(String query, boolean includeInactive);
    Optional<Producto> findById(long id);
    Producto save(Producto producto);
    void updateEstado(long id, EstadoProducto estado);
}
