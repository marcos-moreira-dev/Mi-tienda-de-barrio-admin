package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.compras;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.Compra;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import javafx.geometry.Insets;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Panel reutilizable para búsqueda y listado de compras. */
final class ComprasListPane extends VBox {
    ComprasListPane(TextField busquedaField, AppButton buscarButton, ListView<Compra> listView) {
        super(8, new HBox(8, busquedaField, buscarButton), listView);
        setPadding(new Insets(8));
        setPrefWidth(430);
        HBox.setHgrow(busquedaField, Priority.ALWAYS);
        VBox.setVgrow(listView, Priority.ALWAYS);
    }
}
