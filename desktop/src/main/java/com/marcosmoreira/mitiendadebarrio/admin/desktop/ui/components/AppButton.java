package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components;

import javafx.scene.control.Button;

/** Botón base del sistema. */
public final class AppButton extends Button {

    private AppButton(String text, String styleClass) {
        super(text);
        getStyleClass().addAll("app-button", styleClass);
        setWrapText(true);
        setMinHeight(38);
    }

    public static AppButton primary(String text) {
        return new AppButton(text, "app-button-primary");
    }

    public static AppButton secondary(String text) {
        return new AppButton(text, "app-button-secondary");
    }

    public static AppButton ghost(String text) {
        return new AppButton(text, "app-button-ghost");
    }
}
