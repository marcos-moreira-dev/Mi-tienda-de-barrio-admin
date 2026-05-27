package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

/** Panel lateral de orientación: explica propósito, cuidado y acciones sugeridas. */
public final class InfoPanel extends VBox {
    public InfoPanel(String title, String description, String note, List<String> actions) {
        super(8);
        getStyleClass().add("info-panel");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("info-panel-title");
        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("info-panel-body");
        Label noteLabel = new Label(note);
        noteLabel.setWrapText(true);
        noteLabel.getStyleClass().add("info-panel-note");
        getChildren().addAll(titleLabel, descriptionLabel, noteLabel);
        if (actions != null && !actions.isEmpty()) {
            Label actionsTitle = new Label("Acciones sugeridas");
            actionsTitle.getStyleClass().add("info-panel-title");
            getChildren().add(actionsTitle);
            for (String action : actions) {
                Label actionLabel = new Label("• " + action);
                actionLabel.setWrapText(true);
                actionLabel.getStyleClass().add("info-panel-body");
                getChildren().add(actionLabel);
            }
        }
    }
}
