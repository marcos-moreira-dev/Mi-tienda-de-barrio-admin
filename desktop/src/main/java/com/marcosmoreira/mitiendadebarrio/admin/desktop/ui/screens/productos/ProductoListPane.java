package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.productos;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.Producto;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/** Lista de productos con renderer operativo. */
public final class ProductoListPane extends VBox {
    private final ListView<Producto> listView = new ListView<>();

    public ProductoListPane(Consumer<Producto> onSelected) {
        listView.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                String alerta = item.bajoStock() ? " · BAJO STOCK" : "";
                setText(item.nombre() + " · " + item.categoriaNombre() + " · Stock: " + item.stockActual() + alerta);
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, item) -> onSelected.accept(item));
        getChildren().add(listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
    }

    public void setProductos(List<Producto> productos) {
        listView.getItems().setAll(productos);
    }

    public void clearSelection() {
        listView.getSelectionModel().clearSelection();
    }
}
