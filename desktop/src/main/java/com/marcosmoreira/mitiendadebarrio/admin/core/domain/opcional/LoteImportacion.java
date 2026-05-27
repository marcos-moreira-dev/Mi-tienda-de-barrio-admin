package com.marcosmoreira.mitiendadebarrio.admin.core.domain.opcional;

/** Lote de importación CSV registrado localmente. */
public record LoteImportacion(
        Long id,
        Long plantillaId,
        String tipoImportacion,
        String archivoOrigen,
        String fechaImportacion,
        String estado,
        int totalFilas,
        int filasValidas,
        int filasConError,
        String checksumArchivo,
        String observacion
) {}
