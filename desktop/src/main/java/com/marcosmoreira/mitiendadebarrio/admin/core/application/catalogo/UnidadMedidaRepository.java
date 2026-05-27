package com.marcosmoreira.mitiendadebarrio.admin.core.application.catalogo;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.EstadoCatalogo;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.UnidadMedida;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia para unidades de medida. */
public interface UnidadMedidaRepository {
    List<UnidadMedida> findAll(boolean includeInactive);

    Optional<UnidadMedida> findById(long id);

    UnidadMedida save(UnidadMedida unidadMedida);

    void updateEstado(long id, EstadoCatalogo estado);
}
