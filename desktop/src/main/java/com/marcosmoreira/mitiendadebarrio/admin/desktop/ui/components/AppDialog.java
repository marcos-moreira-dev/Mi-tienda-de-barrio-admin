package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/** Diálogos transversales con texto largo legible y redimensionable. */
public final class AppDialog {

    private AppDialog() {
    }

    public static void info(String title, String header, String content) {
        show(Alert.AlertType.INFORMATION, title, header, content);
    }

    public static void warning(String title, String header, String content) {
        show(Alert.AlertType.WARNING, title, header, content);
    }

    public static void error(String title, String header, String content) {
        show(Alert.AlertType.ERROR, title, header, content);
    }

    private static void show(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setPrefWidth(720);

        TextArea textArea = new TextArea(content == null ? "" : content.strip());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(10);
        textArea.setPrefColumnCount(64);
        textArea.setMinHeight(220);
        alert.getDialogPane().setContent(textArea);
        alert.setResizable(true);
        alert.showAndWait();
    }
}
