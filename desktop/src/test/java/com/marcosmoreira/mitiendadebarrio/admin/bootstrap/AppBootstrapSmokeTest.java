package com.marcosmoreira.mitiendadebarrio.admin.bootstrap;

import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.runtime.RuntimePaths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppBootstrapSmokeTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void limpiarPropiedad() {
        System.clearProperty("mitienda.runtime.root");
    }

    @Test
    void arrancaContextoSinLanzarJavaFx() {
        System.setProperty("mitienda.runtime.root", tempDir.toString());

        AppContext context = AppBootstrap.start();
        RuntimePaths paths = context.paths();

        assertNotNull(context.usuarioLocalService());
        assertNotNull(context.auditoriaService());
        assertNotNull(context.contabilidadBasicaService());
        assertTrue(paths.databaseFile().toFile().exists());
    }
}
