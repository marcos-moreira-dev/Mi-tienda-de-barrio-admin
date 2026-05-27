package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.caja;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja.CajaDiaria;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja.MovimientoCaja;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Panel reutilizable para cajas recientes y movimientos de caja. */
final class CajaListPane extends VBox {
    CajaListPane(ListView<CajaDiaria> cajasView, ListView<MovimientoCaja> movimientosView) {
        super(8, new Label("Cajas recientes"), cajasView, new Label("Movimientos de caja"), movimientosView);
        VBox.setVgrow(cajasView, Priority.ALWAYS);
        VBox.setVgrow(movimientosView, Priority.ALWAYS);
    }
}
