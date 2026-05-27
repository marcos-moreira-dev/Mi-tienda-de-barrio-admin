package com.marcosmoreira.mitiendadebarrio.admin.core.domain.opcional;

public record RegistroLoteImportacion(
        Long plantillaId,
        String tipoImportacion,
        String archivoOrigen,
        int totalFilas,
        int filasValidas,
        int filasConError,
        String checksumArchivo,
        String observacion
) {}
