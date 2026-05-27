package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components;

import javafx.scene.control.Label;

public final class StatusBadge extends Label {
    public StatusBadge(String text) {
        super(text);
        getStyleClass().add("status-badge");
    }
}
