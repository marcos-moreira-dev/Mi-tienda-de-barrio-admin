package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.fiado;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.fiado.FiadoService;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja.MetodoPagoCaja;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiado.*;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.*;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.math.BigDecimal;
import java.util.List;

/** Vista de fiado y cuentas por cobrar. */
public final class FiadoCuentasView {
    private final FiadoService service;
    private final ListView<ClienteFiado> clientesView = new ListView<>();
    private final ListView<CuentaPorCobrar> cuentasView = new ListView<>();
    private final TextField nombreField = new TextField();
    private final TextField telefonoField = new TextField();
    private final TextField direccionField = new TextField();
    private final TextField limiteField = new TextField("0");
    private final TextArea observacionArea = new TextArea();
    private final TextField montoCuentaField = new TextField();
    private final TextField abonoField = new TextField();
    private ClienteFiado clienteSeleccionado;
    private CuentaPorCobrar cuentaSeleccionada;

    public FiadoCuentasView(AppContext context) { this.service = context.fiadoService(); }

    public Node render() {
        configurarListas();
        cargarClientes();
        observacionArea.setPrefRowCount(3);
        AppButton guardarCliente = AppButton.primary("Guardar cliente");
        guardarCliente.setOnAction(e -> guardarCliente());
        AppButton nuevaCuenta = AppButton.secondary("Abrir cuenta");
        nuevaCuenta.setOnAction(e -> abrirCuenta());
        AppButton abonar = AppButton.ghost("Registrar abono");
        abonar.setOnAction(e -> abonar());

        Node form = new FiadoFormPane(
                nombreField, telefonoField, direccionField, limiteField, observacionArea, guardarCliente,
                montoCuentaField, nuevaCuenta, abonoField, abonar
        );
        Node center = new FiadoListPane(clientesView, cuentasView);

        InfoPanel side = new InfoPanel("Fiado",
                "Módulo opcional para negocios que sí venden fiado.",
                "No es cartera financiera avanzada. Sirve para registrar deuda simple, abonos y saldo.",
                List.of("No active este módulo si el negocio no usa fiado.", "Cada abono actualiza el saldo.", "Evita confiar solo en cuaderno o memoria."));
        side.setPrefWidth(330);
        return new ModuleScaffold("Fiado / cuentas por cobrar", "Clientes, deudas simples, abonos y saldos pendientes.", new AppCard(new HBox(12, center, form)), side, null);
    }

    private void configurarListas() {
        clientesView.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(ClienteFiado item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.nombre() + " · saldo $" + item.saldoPendiente());
            }
        });
        cuentasView.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(CuentaPorCobrar item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "#" + item.id() + " · saldo $" + item.saldoPendiente() + " · " + item.estado().label());
            }
        });
        clientesView.getSelectionModel().selectedItemProperty().addListener((obs, old, item) -> seleccionarCliente(item));
        cuentasView.getSelectionModel().selectedItemProperty().addListener((obs, old, item) -> cuentaSeleccionada = item);
    }

    private void cargarClientes() { clientesView.getItems().setAll(service.buscarClientes("", false)); }
    private void seleccionarCliente(ClienteFiado item) {
        clienteSeleccionado = item;
        if (item == null) { cuentasView.getItems().clear(); return; }
        nombreField.setText(item.nombre());
        telefonoField.setText(item.telefono());
        direccionField.setText(item.direccion());
        limiteField.setText(item.limiteCredito().toPlainString());
        observacionArea.setText(item.observacion());
        cuentasView.getItems().setAll(service.cuentasAbiertas(item.id()));
    }
    private void guardarCliente() {
        ClienteFiado cliente = new ClienteFiado(
                clienteSeleccionado == null ? null : clienteSeleccionado.id(),
                nombreField.getText(), telefonoField.getText(), direccionField.getText(), decimal(limiteField.getText()),
                clienteSeleccionado == null ? EstadoClienteFiado.ACTIVO : clienteSeleccionado.estado(),
                observacionArea.getText(), BigDecimal.ZERO);
        OperationResult<ClienteFiado> result = service.guardarCliente(cliente);
        mostrar(result, "Fiado");
        cargarClientes();
    }
    private void abrirCuenta() {
        OperationResult<CuentaPorCobrar> result = service.abrirCuenta(clienteSeleccionado == null ? null : clienteSeleccionado.id(), decimal(montoCuentaField.getText()), "Cuenta abierta manualmente.");
        mostrar(result, "Cuenta por cobrar");
        if (clienteSeleccionado != null) seleccionarCliente(clienteSeleccionado);
        cargarClientes();
    }
    private void abonar() {
        OperationResult<?> result = service.registrarAbono(cuentaSeleccionada == null ? null : cuentaSeleccionada.id(), decimal(abonoField.getText()), MetodoPagoCaja.EFECTIVO, "Abono manual.");
        mostrar(result, "Abono");
        if (clienteSeleccionado != null) seleccionarCliente(clienteSeleccionado);
        cargarClientes();
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
