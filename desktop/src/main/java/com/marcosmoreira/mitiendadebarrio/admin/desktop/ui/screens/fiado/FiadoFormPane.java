package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.fiado;

import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** Panel de formulario para cliente, cuenta y abono de fiado. */
final class FiadoFormPane extends VBox {
    FiadoFormPane(
            TextField nombreField,
            TextField telefonoField,
            TextField direccionField,
            TextField limiteField,
            TextArea observacionArea,
            AppButton guardarCliente,
            TextField montoCuentaField,
            AppButton nuevaCuenta,
            TextField abonoField,
            AppButton abonar
    ) {
        super(8,
                new Label("Nombre *"), nombreField,
                new Label("Teléfono"), telefonoField,
                new Label("Dirección"), direccionField,
                new Label("Límite crédito"), limiteField,
                new Label("Observación"), observacionArea,
                guardarCliente,
                new Separator(),
                new Label("Nueva cuenta"), montoCuentaField, nuevaCuenta,
                new Label("Abono a cuenta seleccionada"), abonoField, abonar
        );
        setPadding(new Insets(8));
        setPrefWidth(330);
    }
}
