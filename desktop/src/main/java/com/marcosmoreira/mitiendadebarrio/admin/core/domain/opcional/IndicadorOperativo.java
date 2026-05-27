package com.marcosmoreira.mitiendadebarrio.admin.core.domain.opcional;

/** Indicador operativo visible en paneles/reportes locales. */
public record IndicadorOperativo(Long id, String codigo, String nombre, String descripcion, String modulo, int ordenVisual, boolean activo) {}
