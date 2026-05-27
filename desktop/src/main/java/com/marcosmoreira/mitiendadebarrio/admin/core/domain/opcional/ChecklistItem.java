package com.marcosmoreira.mitiendadebarrio.admin.core.domain.opcional;

/** Ítem individual de checklist operativo. */
public record ChecklistItem(Long id, Long checklistId, int orden, String titulo, String descripcion, String moduloRelacionado, boolean activo) {}
