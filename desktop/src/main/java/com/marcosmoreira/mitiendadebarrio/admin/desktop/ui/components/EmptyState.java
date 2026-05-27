package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public final class EmptyState extends VBox {
    public EmptyState(String title, String message) {
        super(8);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("empty-title");
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("empty-message");
        getChildren().addAll(titleLabel, messageLabel);
        getStyleClass().add("empty-state");
    }
}
