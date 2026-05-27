package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.runtime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuntimePathsTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void limpiarPropiedad() {
        System.clearProperty("mitienda.runtime.root");
    }

    @Test
    void usaRuntimeRootExplicitoParaDesarrolloLocal() {
        System.setProperty("mitienda.runtime.root", tempDir.toString());

        RuntimePaths paths = RuntimePaths.defaultForUserHome();

        assertEquals(tempDir, paths.rootDirectory());
        assertEquals(tempDir.resolve("data"), paths.dataDirectory());
        assertEquals(tempDir.resolve("data").resolve("mi_tienda_de_barrio_admin.sqlite"), paths.databaseFile());
    }
}
