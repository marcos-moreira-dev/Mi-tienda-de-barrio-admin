package com.marcosmoreira.mitiendadebarrio.admin.core.application.catalogo;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.EstadoCatalogo;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.Marca;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia para marcas. */
public interface MarcaRepository {
    List<Marca> findAll(boolean includeInactive);

    Optional<Marca> findById(long id);

    Marca save(Marca marca);

    void updateEstado(long id, EstadoCatalogo estado);
}
