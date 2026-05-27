package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Collection;

/** Fábrica mínima de campos para no repetir estilos y tamaños en formularios. */
public final class AppFormFactory {

    private AppFormFactory() {
    }

    public static TextField textField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    public static PasswordField passwordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    public static TextArea textArea(String prompt, int rows) {
        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setWrapText(true);
        area.setPrefRowCount(Math.max(2, rows));
        area.setMaxWidth(Double.MAX_VALUE);
        return area;
    }

    public static <T> ComboBox<T> comboBox(Collection<T> values) {
        ComboBox<T> combo = new ComboBox<>();
        if (values != null) {
            combo.getItems().addAll(values);
        }
        combo.setMaxWidth(Double.MAX_VALUE);
        return combo;
    }

    public static DatePicker datePicker(String prompt) {
        DatePicker picker = new DatePicker();
        picker.setPromptText(prompt);
        picker.setMaxWidth(Double.MAX_VALUE);
        return picker;
    }

    public static CheckBox checkBox(String text) {
        CheckBox checkBox = new CheckBox(text);
        checkBox.getStyleClass().add("app-check-box");
        return checkBox;
    }
}
