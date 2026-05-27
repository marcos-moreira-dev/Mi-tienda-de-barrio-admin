package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/** Barra de acciones inferior o superior, con alineación consistente. */
public final class ActionBar extends HBox {
    public ActionBar(Node... actions) {
        super(8);
        setAlignment(Pos.CENTER_RIGHT);
        getStyleClass().add("action-bar");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);
        getChildren().addAll(actions);
    }
}
