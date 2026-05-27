package com.marcosmoreira.mitiendadebarrio.admin.core.domain.opcional;

/** Plantilla simple para importación CSV controlada. */
public record PlantillaImportacion(Long id, String codigo, String nombre, String tipoImportacion, String encabezadosCsv, String descripcion, boolean activo) {}
