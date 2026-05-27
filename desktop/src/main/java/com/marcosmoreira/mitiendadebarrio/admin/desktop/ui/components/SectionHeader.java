package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public final class SectionHeader extends VBox {

    public SectionHeader(String title, String subtitle) {
        super(4);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("section-subtitle");
        getChildren().addAll(titleLabel, subtitleLabel);
    }
}
