package com.marcosmoreira.mitiendadebarrio.admin.core.application.catalogo;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.Categoria;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.EstadoCatalogo;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia para categorías. */
public interface CategoriaRepository {
    List<Categoria> findAll(boolean includeInactive);

    Optional<Categoria> findById(long id);

    Categoria save(Categoria categoria);

    void updateEstado(long id, EstadoCatalogo estado);
}
