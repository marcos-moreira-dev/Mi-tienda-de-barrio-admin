package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/** Listas visuales reutilizables: bullets, pasos numerados y filas compactas. */
public final class AppListFactory {

    private AppListFactory() {
    }

    public static VBox bulletList(List<String> items, String rowStyleClass, String textStyleClass) {
        VBox rows = new VBox(8);
        if (items == null) {
            return rows;
        }
        for (String item : items) {
            Label bullet = new Label("•");
            bullet.getStyleClass().add("app-list-bullet");
            Label text = new Label(item == null ? "" : item);
            text.setWrapText(true);
            if (textStyleClass != null && !textStyleClass.isBlank()) {
                text.getStyleClass().add(textStyleClass);
            }
            HBox row = new HBox(8, bullet, text);
            row.setAlignment(Pos.CENTER_LEFT);
            if (rowStyleClass != null && !rowStyleClass.isBlank()) {
                row.getStyleClass().add(rowStyleClass);
            }
            HBox.setHgrow(text, Priority.ALWAYS);
            rows.getChildren().add(row);
        }
        return rows;
    }

    public static VBox numberedSteps(List<String> steps, String rowStyleClass, String numberStyleClass, String textStyleClass) {
        VBox rows = new VBox(8);
        if (steps == null) {
            return rows;
        }
        int index = 1;
        for (String step : steps) {
            Label number = new Label(String.valueOf(index));
            if (numberStyleClass != null && !numberStyleClass.isBlank()) {
                number.getStyleClass().add(numberStyleClass);
            }
            Label text = new Label(step == null ? "" : step);
            text.setWrapText(true);
            if (textStyleClass != null && !textStyleClass.isBlank()) {
                text.getStyleClass().add(textStyleClass);
            }
            HBox row = new HBox(10, number, text);
            row.setAlignment(Pos.CENTER_LEFT);
            if (rowStyleClass != null && !rowStyleClass.isBlank()) {
                row.getStyleClass().add(rowStyleClass);
            }
            HBox.setHgrow(text, Priority.ALWAYS);
            rows.getChildren().add(row);
            index++;
        }
        return rows;
    }
}
