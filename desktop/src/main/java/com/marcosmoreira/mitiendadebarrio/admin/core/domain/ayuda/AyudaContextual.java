package com.marcosmoreira.mitiendadebarrio.admin.core.domain.ayuda;

/** Entrada de mini manual o ayuda contextual por módulo. */
public record AyudaContextual(Long id, String modulo, String titulo, String contenido, Integer orden, boolean visible) { }
