package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/** Grid de formulario con etiquetas consistentes. */
public final class FormGrid extends GridPane {

    private int rowIndex = 0;

    public FormGrid() {
        setHgap(12);
        setVgap(10);
        setPadding(new Insets(4));
        getStyleClass().add("form-grid");
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(140);
        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setHgrow(Priority.ALWAYS);
        getColumnConstraints().addAll(labelColumn, fieldColumn);
    }

    public void addField(String label, Node field) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("form-label");
        add(labelNode, 0, rowIndex);
        add(field, 1, rowIndex);
        rowIndex++;
    }
}
