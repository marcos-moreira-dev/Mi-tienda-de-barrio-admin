package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Fábrica de scrolls para mantener políticas consistentes entre pantallas. */
public final class AppScrollFactory {

    private AppScrollFactory() {
    }

    public static ScrollPane vertical(Node content, String styleClass) {
        ScrollPane scroll = base(content, styleClass);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    public static ScrollPane horizontal(Node content, String styleClass, double preferredHeight) {
        ScrollPane scroll = base(content, styleClass);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefHeight(preferredHeight);
        return scroll;
    }

    public static ScrollPane both(Node content, String styleClass) {
        ScrollPane scroll = base(content, styleClass);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    private static ScrollPane base(Node content, String styleClass) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.getStyleClass().add("app-scroll");
        if (styleClass != null && !styleClass.isBlank()) {
            scroll.getStyleClass().add(styleClass);
        }
        scroll.setPannable(true);
        return scroll;
    }
}
