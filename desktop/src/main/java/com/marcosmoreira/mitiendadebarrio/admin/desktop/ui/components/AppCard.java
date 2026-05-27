package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

/** Tarjeta visual reutilizable. */
public final class AppCard extends VBox {
    public AppCard(Node... children) {
        super(12, children);
        getStyleClass().add("app-card");
    }
}
