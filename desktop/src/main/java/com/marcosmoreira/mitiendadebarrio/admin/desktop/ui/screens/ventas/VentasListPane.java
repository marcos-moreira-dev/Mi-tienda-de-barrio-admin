package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.ventas;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.VentaInterna;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import javafx.geometry.Insets;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Panel reutilizable para búsqueda y listado de salidas/ventas internas. */
final class VentasListPane extends VBox {
    VentasListPane(TextField busquedaField, AppButton buscarButton, ListView<VentaInterna> listView) {
        super(8, new HBox(8, busquedaField, buscarButton), listView);
        setPadding(new Insets(8));
        setPrefWidth(430);
        HBox.setHgrow(busquedaField, Priority.ALWAYS);
        VBox.setVgrow(listView, Priority.ALWAYS);
    }
}
