package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.productos;

import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppFormFactory;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Filtros de búsqueda para catálogo de productos. */
public final class ProductoFilterPane extends VBox {
    private final TextField busquedaField = AppFormFactory.textField("Buscar por nombre, código o categoría");
    private final CheckBox incluirInactivosCheck = AppFormFactory.checkBox("Mostrar inactivos");

    public ProductoFilterPane(Runnable onSearch) {
        AppButton buscarButton = AppButton.secondary("Buscar");
        buscarButton.setOnAction(event -> onSearch.run());
        busquedaField.setOnAction(event -> onSearch.run());
        incluirInactivosCheck.setOnAction(event -> onSearch.run());

        HBox searchRow = new HBox(8, busquedaField, buscarButton);
        HBox.setHgrow(busquedaField, Priority.ALWAYS);
        setPadding(new Insets(0));
        setSpacing(8);
        getChildren().addAll(searchRow, incluirInactivosCheck);
    }

    public String query() {
        return busquedaField.getText();
    }

    public boolean includeInactive() {
        return incluirInactivosCheck.isSelected();
    }
}
