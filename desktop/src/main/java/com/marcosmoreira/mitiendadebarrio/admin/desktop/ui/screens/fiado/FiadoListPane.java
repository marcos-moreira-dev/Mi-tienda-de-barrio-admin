package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.fiado;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiado.ClienteFiado;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiado.CuentaPorCobrar;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Panel reutilizable para clientes de fiado y cuentas abiertas. */
final class FiadoListPane extends VBox {
    FiadoListPane(ListView<ClienteFiado> clientesView, ListView<CuentaPorCobrar> cuentasView) {
        super(8, new Label("Clientes de fiado"), clientesView, new Label("Cuentas abiertas del cliente"), cuentasView);
        VBox.setVgrow(clientesView, Priority.ALWAYS);
        VBox.setVgrow(cuentasView, Priority.ALWAYS);
    }
}
