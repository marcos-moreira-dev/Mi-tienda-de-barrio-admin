package com.marcosmoreira.mitiendadebarrio.admin.core.domain.opcional;

import java.util.List;

/** Checklist operativo para rutinas de tienda. */
public record ChecklistOperativo(Long id, String codigo, String nombre, String descripcion, String frecuencia, boolean activo, List<ChecklistItem> items) {}
