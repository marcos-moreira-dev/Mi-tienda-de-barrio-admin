package com.marcosmoreira.mitiendadebarrio.admin.core.domain.opcional;

/** Error o advertencia detectada en una importación CSV. */
public record ErrorImportacion(Long id, Long loteImportacionId, int numeroFila, String campo, String valorOriginal, String mensaje, String severidad) {}
