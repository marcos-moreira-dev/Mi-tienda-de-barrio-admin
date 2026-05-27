package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.caja;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja.TipoMovimientoCaja;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** Panel de formulario para apertura, movimientos y cierre de caja diaria. */
final class CajaFormPane extends VBox {
    CajaFormPane(
            TextField saldoInicialField,
            AppButton abrir,
            ComboBox<TipoMovimientoCaja> tipoCombo,
            TextField montoField,
            TextField descripcionField,
            AppButton registrar,
            TextField saldoContadoField,
            AppButton cerrar
    ) {
        super(8,
                new Label("Abrir caja"), saldoInicialField, abrir, new Separator(),
                new Label("Movimiento"), tipoCombo, montoField, descripcionField, registrar, new Separator(),
                new Label("Cierre"), saldoContadoField, cerrar
        );
        setPadding(new Insets(8));
        setPrefWidth(320);
    }
}
