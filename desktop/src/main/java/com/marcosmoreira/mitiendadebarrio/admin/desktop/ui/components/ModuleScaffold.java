package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

/** Estructura base para módulos: encabezado + contenido central + ayuda/acciones. */
public final class ModuleScaffold extends BorderPane {
    public ModuleScaffold(String title, String subtitle, Node content, Node sidePanel, Node footer) {
        getStyleClass().add("module-scaffold");
        setTop(new SectionHeader(title, subtitle));
        setCenter(content);
        if (sidePanel != null) {
            setRight(sidePanel);
        }
        if (footer != null) {
            setBottom(footer);
        }
    }
}
