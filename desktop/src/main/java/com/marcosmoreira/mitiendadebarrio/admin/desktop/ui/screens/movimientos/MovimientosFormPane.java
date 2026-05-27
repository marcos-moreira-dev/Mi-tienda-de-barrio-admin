package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.movimientos;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.movimiento.TipoMovimientoInventario;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.Producto;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.FormGrid;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Panel de formulario para ajustes y mermas de inventario. */
final class MovimientosFormPane extends VBox {
    MovimientosFormPane(
            ComboBox<Producto> productoCombo,
            ComboBox<TipoMovimientoInventario> tipoCombo,
            TextField cantidadField,
            TextField motivoField,
            TextField responsableField,
            TextArea observacionArea,
            AppButton limpiarButton,
            AppButton registrarButton
    ) {
        super(10);
        FormGrid formGrid = new FormGrid();
        formGrid.addField("Producto *", productoCombo);
        formGrid.addField("Tipo *", tipoCombo);
        formGrid.addField("Cantidad *", cantidadField);
        formGrid.addField("Motivo *", motivoField);
        formGrid.addField("Responsable", responsableField);
        getChildren().addAll(formGrid, new Label("Observación"), observacionArea, new HBox(8, limpiarButton, registrarButton));
        setPadding(new Insets(8));
        HBox.setHgrow(this, Priority.ALWAYS);
    }
}
