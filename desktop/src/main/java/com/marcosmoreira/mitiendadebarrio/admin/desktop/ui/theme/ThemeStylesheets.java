package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.theme;

import javafx.scene.Scene;

import java.net.URL;
import java.util.List;

/**
 * Carga ordenada de hojas de estilo JavaFX.
 *
 * <p>La regla de arquitectura para R7 es simple: la aplicación ya no depende de
 * una sábana única {@code app.css}. Cada capa visual tiene un archivo propio y
 * se carga en orden estable para que la cascada sea predecible.</p>
 *
 * <p>Orden actual:</p>
 * <ol>
 *     <li>{@code tokens.css}: colores, tipografía y sombras.</li>
 *     <li>{@code base.css}: reglas globales mínimas de escena.</li>
 *     <li>{@code controls.css}: normalización de controles JavaFX.</li>
 *     <li>{@code components.css}: componentes reutilizables propios.</li>
 *     <li>{@code login.css}: login y loading.</li>
 *     <li>{@code shell.css}: menú, sidebar, topbar, workspace y statusbar.</li>
 *     <li>{@code use-cases.css}: hub de casos de uso.</li>
 *     <li>{@code modules.css}: ajustes específicos de módulos de negocio.</li>
 * </ol>
 */
public final class ThemeStylesheets {

    private static final List<String> STYLESHEETS = List.of(
            "/styles/tokens.css",
            "/styles/base.css",
            "/styles/controls.css",
            "/styles/components.css",
            "/styles/login.css",
            "/styles/shell.css",
            "/styles/use-cases.css",
            "/styles/modules.css"
    );

    private ThemeStylesheets() {
        // Utility class: no estado, no instanciación.
    }

    /**
     * Aplica el tema completo a una escena.
     *
     * @param scene escena JavaFX ya creada
     * @throws IllegalStateException si falta alguna hoja de estilos esperada
     */
    public static void applyTo(Scene scene) {
        scene.getStylesheets().clear();
        for (String stylesheet : STYLESHEETS) {
            URL resource = ThemeStylesheets.class.getResource(stylesheet);
            if (resource == null) {
                throw new IllegalStateException("No se encontró la hoja de estilo: " + stylesheet);
            }
            scene.getStylesheets().add(resource.toExternalForm());
        }
    }
}
