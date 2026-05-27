package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.runtime;

import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Crea carpetas locales necesarias para operar sin servidor. */
public final class LocalRuntimeInitializer {

    private final RuntimePaths paths;

    public LocalRuntimeInitializer(RuntimePaths paths) {
        this.paths = paths;
    }

    public void ensureDirectories() {
        List<Path> required = List.of(
                paths.rootDirectory(),
                paths.dataDirectory(),
                paths.backupsDirectory(),
                paths.reportsDirectory(),
                paths.productImagesDirectory(),
                paths.logsDirectory(),
                paths.configDirectory(),
                paths.licenseDirectory()
        );

        for (Path path : required) {
            try {
                Files.createDirectories(path);
            } catch (IOException ex) {
                throw new InfrastructureException("No se pudo preparar carpeta local: " + path, ex);
            }
        }
    }
}
