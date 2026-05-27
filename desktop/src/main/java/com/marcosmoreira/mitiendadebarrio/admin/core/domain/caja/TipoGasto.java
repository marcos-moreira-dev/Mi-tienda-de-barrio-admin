package com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja;

/** Categoría simple de gasto operativo para caja local. */
public record TipoGasto(Long id, String nombre, String descripcion, boolean activo) { }
