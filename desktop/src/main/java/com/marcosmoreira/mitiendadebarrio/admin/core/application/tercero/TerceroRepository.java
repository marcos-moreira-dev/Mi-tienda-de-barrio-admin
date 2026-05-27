package com.marcosmoreira.mitiendadebarrio.admin.core.application.tercero;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.tercero.EstadoTercero;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.tercero.Tercero;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia para terceros locales. */
public interface TerceroRepository {
    List<Tercero> findAll(boolean includeInactive);
    Optional<Tercero> findById(long id);
    Tercero save(Tercero tercero);
    void updateEstado(long id, EstadoTercero estado);
    void asegurarCliente(long terceroId, boolean permiteFiado);
    void asegurarProveedor(long terceroId);
}
