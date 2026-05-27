package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.ventas;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.Producto;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.MetodoPagoVentaInterna;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.FormGrid;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Panel de formulario para salidas internas de stock. */
final class VentasFormPane extends VBox {
    VentasFormPane(
            ComboBox<Producto> productoCombo,
            TextField cantidadField,
            TextField precioField,
            ComboBox<MetodoPagoVentaInterna> metodoPagoCombo,
            TextField referenciaField,
            CheckBox advertenciaCheck,
            TextArea observacionArea,
            AppButton limpiarButton,
            AppButton registrarButton
    ) {
        super(10);
        FormGrid formGrid = new FormGrid();
        formGrid.addField("Producto *", productoCombo);
        formGrid.addField("Cantidad *", cantidadField);
        formGrid.addField("Precio unitario", precioField);
        formGrid.addField("Método de pago", metodoPagoCombo);
        formGrid.addField("Referencia", referenciaField);
        getChildren().addAll(formGrid, advertenciaCheck, new Label("Observación"), observacionArea, new HBox(8, limpiarButton, registrarButton));
        setPadding(new Insets(8));
        HBox.setHgrow(this, Priority.ALWAYS);
    }
}
