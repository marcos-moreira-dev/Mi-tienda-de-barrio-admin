package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.caja;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.caja.CajaDiariaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja.*;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.*;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.math.BigDecimal;
import java.util.List;

/** Vista de caja diaria opcional. */
public final class CajaDiariaView {
    private final CajaDiariaService service;
    private final ListView<CajaDiaria> cajasView = new ListView<>();
    private final ListView<MovimientoCaja> movimientosView = new ListView<>();
    private final TextField saldoInicialField = new TextField("0");
    private final TextField montoField = new TextField();
    private final TextField descripcionField = new TextField();
    private final TextField saldoContadoField = new TextField();
    private final ComboBox<TipoMovimientoCaja> tipoCombo = new ComboBox<>();
    private CajaDiaria seleccionada;

    public CajaDiariaView(AppContext context) { this.service = context.cajaDiariaService(); }

    public Node render() {
        configurarListas();
        cargarCajas();
        tipoCombo.getItems().setAll(TipoMovimientoCaja.INGRESO, TipoMovimientoCaja.EGRESO);
        tipoCombo.setValue(TipoMovimientoCaja.INGRESO);
        saldoInicialField.setPromptText("Saldo inicial del día");
        montoField.setPromptText("Monto");
        descripcionField.setPromptText("Descripción obligatoria");
        saldoContadoField.setPromptText("Saldo contado al cerrar");

        AppButton abrir = AppButton.primary("Abrir caja de hoy");
        abrir.setOnAction(e -> abrirCaja());
        AppButton registrar = AppButton.secondary("Registrar movimiento");
        registrar.setOnAction(e -> registrarMovimiento());
        AppButton cerrar = AppButton.ghost("Cerrar caja");
        cerrar.setOnAction(e -> cerrarCaja());

        Node form = new CajaFormPane(saldoInicialField, abrir, tipoCombo, montoField, descripcionField, registrar, saldoContadoField, cerrar);
        Node center = new CajaListPane(cajasView, movimientosView);

        InfoPanel side = new InfoPanel("Caja diaria",
                "Módulo opcional para control operativo de efectivo.",
                "No reemplaza contabilidad formal. Sirve para registrar ingresos, egresos y cierre.",
                List.of("Use descripciones claras.", "Cierre la caja antes de respaldo diario.", "Las ventas internas aún no se integran automáticamente a caja."));
        side.setPrefWidth(330);
        return new ModuleScaffold("Caja diaria", "Apertura, ingresos, egresos y cierre operativo.", new AppCard(new HBox(12, center, form)), side, null);
    }

    private void configurarListas() {
        cajasView.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(CajaDiaria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.fecha() + " · " + item.estado().label() + " · esperado $" + item.saldoEsperado());
            }
        });
        movimientosView.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(MovimientoCaja item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.tipoMovimiento().label() + " $" + item.monto() + " · " + item.descripcion());
            }
        });
        cajasView.getSelectionModel().selectedItemProperty().addListener((obs, old, item) -> seleccionar(item));
    }

    private void cargarCajas() { cajasView.getItems().setAll(service.recientes()); }
    private void seleccionar(CajaDiaria caja) {
        seleccionada = caja;
        movimientosView.getItems().setAll(caja == null ? List.of() : service.movimientos(caja.id()));
    }
    private void abrirCaja() {
        OperationResult<CajaDiaria> result = service.abrirHoy(decimal(saldoInicialField.getText()), "Apertura manual desde módulo Caja.");
        mostrar(result, "Caja diaria");
        cargarCajas();
    }
    private void registrarMovimiento() {
        OperationResult<MovimientoCaja> result = service.registrarMovimiento(seleccionada == null ? null : seleccionada.id(), tipoCombo.getValue(), decimal(montoField.getText()), MetodoPagoCaja.EFECTIVO, descripcionField.getText());
        mostrar(result, "Movimiento de caja");
        cargarCajas();
        if (seleccionada != null) seleccionar(seleccionada);
    }
    private void cerrarCaja() {
        OperationResult<CajaDiaria> result = service.cerrar(seleccionada == null ? null : seleccionada.id(), decimal(saldoContadoField.getText()), "Cierre manual desde módulo Caja.");
        mostrar(result, "Cierre de caja");
        cargarCajas();
    }
    private BigDecimal decimal(String text) {
        if (text == null || text.isBlank()) return BigDecimal.ZERO;
        return new BigDecimal(text.trim().replace(",", "."));
    }
    private void mostrar(OperationResult<?> result, String title) {
        if (result.success()) AppDialog.info(title, "Operación completada", result.message());
        else AppDialog.warning(title, "Revise la operación", result.message());
    }
}
