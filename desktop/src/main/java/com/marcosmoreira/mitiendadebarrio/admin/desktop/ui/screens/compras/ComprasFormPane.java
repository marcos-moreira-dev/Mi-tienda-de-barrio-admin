package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.compras;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.TipoComprobanteCompra;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.Producto;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.proveedor.Proveedor;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.FormGrid;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Panel de formulario para registrar entradas de mercadería. */
final class ComprasFormPane extends VBox {
    ComprasFormPane(
            ComboBox<Proveedor> proveedorCombo,
            ComboBox<Producto> productoCombo,
            TextField cantidadField,
            TextField costoField,
            DatePicker fechaCompraPicker,
            ComboBox<TipoComprobanteCompra> tipoComprobanteCombo,
            TextField numeroComprobanteField,
            TextField codigoLoteField,
            DatePicker fechaVencimientoPicker,
            TextArea observacionArea,
            AppButton limpiarButton,
            AppButton registrarButton
    ) {
        super(10);
        FormGrid formGrid = new FormGrid();
        formGrid.addField("Proveedor", proveedorCombo);
        formGrid.addField("Producto *", productoCombo);
        formGrid.addField("Cantidad *", cantidadField);
        formGrid.addField("Costo unitario", costoField);
        formGrid.addField("Fecha compra", fechaCompraPicker);
        formGrid.addField("Tipo comprobante", tipoComprobanteCombo);
        formGrid.addField("Número comprobante", numeroComprobanteField);
        formGrid.addField("Código lote", codigoLoteField);
        formGrid.addField("Vencimiento", fechaVencimientoPicker);
        getChildren().addAll(formGrid, new Label("Observación"), observacionArea, new HBox(8, limpiarButton, registrarButton));
        setPadding(new Insets(8));
        HBox.setHgrow(this, Priority.ALWAYS);
    }
}
