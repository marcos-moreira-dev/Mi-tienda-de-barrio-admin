package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.runtime;

import java.nio.file.Path;

/** Rutas locales de ejecución de la aplicación autocontenida. */
public record RuntimePaths(
        Path rootDirectory,
        Path dataDirectory,
        Path backupsDirectory,
        Path reportsDirectory,
        Path productImagesDirectory,
        Path logsDirectory,
        Path configDirectory,
        Path licenseDirectory,
        Path databaseFile
) {
    public static RuntimePaths defaultForUserHome() {
        Path root = resolveRootDirectory();
        Path data = root.resolve("data");
        return new RuntimePaths(
                root,
                data,
                root.resolve("backups"),
                root.resolve("reports"),
                root.resolve("images").resolve("products"),
                root.resolve("logs"),
                root.resolve("config"),
                root.resolve("license"),
                data.resolve("mi_tienda_de_barrio_admin.sqlite")
        );
    }

    private static Path resolveRootDirectory() {
        String explicitRoot = firstNonBlank(
                System.getProperty("mitienda.runtime.root"),
                System.getenv("MITIENDA_RUNTIME_ROOT")
        );
        if (explicitRoot != null) {
            return Path.of(explicitRoot);
        }

        String profile = firstNonBlank(
                System.getProperty("mitienda.runtime.profile"),
                System.getenv("MITIENDA_RUNTIME_PROFILE")
        );
        String folderName = "presentacion".equalsIgnoreCase(profile)
                ? ".mi-tienda-de-barrio-admin-presentacion"
                : ".mi-tienda-de-barrio-admin";
        return Path.of(System.getProperty("user.home"), folderName);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.strip();
            }
        }
        return null;
    }
}
