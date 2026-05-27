package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.movimientos;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.movimiento.MovimientoInventario;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.movimiento.TipoMovimientoInventario;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.Producto;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.*;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.List;

/** Vista de movimientos de inventario y ajustes manuales. */
public final class MovimientosInventarioView {
    private final AppContext context;
    private final ListView<MovimientoInventario> listView = new ListView<>();
    private final TextField busquedaField = new TextField();
    private final ComboBox<Producto> productoCombo = new ComboBox<>();
    private final ComboBox<TipoMovimientoInventario> tipoCombo = new ComboBox<>();
    private final TextField cantidadField = new TextField("1");
    private final TextField motivoField = new TextField();
    private final TextField responsableField = new TextField();
    private final TextArea observacionArea = new TextArea();

    public MovimientosInventarioView(AppContext context) { this.context = context; }

    public Node render() {
        configurarLista();
        configurarCombos();
        cargarDatos();

        busquedaField.setPromptText("Buscar por producto, tipo o motivo");
        motivoField.setPromptText("Motivo obligatorio: conteo físico, daño, vencimiento, corrección...");
        responsableField.setPromptText("Responsable opcional");
        observacionArea.setPromptText("Observación opcional para trazabilidad humana.");
        observacionArea.setPrefRowCount(3);
        observacionArea.setWrapText(true);

        AppButton buscarButton = AppButton.secondary("Buscar");
        buscarButton.setOnAction(event -> cargarDatos());
        AppButton registrarButton = AppButton.primary("Registrar movimiento");
        registrarButton.setOnAction(event -> registrar());
        AppButton limpiarButton = AppButton.ghost("Limpiar");
        limpiarButton.setOnAction(event -> limpiar());

        Node listado = new MovimientosListPane(busquedaField, buscarButton, listView);
        Node formulario = new MovimientosFormPane(
                productoCombo, tipoCombo, cantidadField, motivoField, responsableField,
                observacionArea, limpiarButton, registrarButton
        );

        InfoPanel info = new InfoPanel(
                "Trazabilidad de stock",
                "Este módulo registra ajustes manuales y mermas. Compras y ventas internas tienen flujos propios.",
                "Cada movimiento guarda stock anterior, stock nuevo, motivo y referencia para auditoría humana.",
                List.of("No permite dejar stock negativo.", "Corrección interpreta la cantidad como nuevo stock.", "Merma/retiro también alimenta el registro de pérdidas.")
        );
        info.setPrefWidth(330);

        return new ModuleScaffold(
                "Movimientos de inventario",
                "Ajustes, correcciones y mermas con trazabilidad local.",
                new AppCard(new HBox(12, listado, formulario)),
                info,
                null
        );
    }

    private void configurarLista() {
        listView.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(MovimientoInventario item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item.productoNombre() + " · " + item.tipoMovimiento().label() + " · " + item.cantidad()
                        + " · " + item.stockAnterior() + " → " + item.stockNuevo());
            }
        });
    }

    private void configurarCombos() {
        productoCombo.getItems().setAll(context.productoService().listar("", false));
        productoCombo.setCellFactory(view -> productoCell());
        productoCombo.setButtonCell(productoCell());
        tipoCombo.getItems().setAll(TipoMovimientoInventario.AJUSTE_POSITIVO, TipoMovimientoInventario.AJUSTE_NEGATIVO,
                TipoMovimientoInventario.MERMA, TipoMovimientoInventario.RETIRO_VENCIMIENTO, TipoMovimientoInventario.CORRECCION);
        tipoCombo.setCellFactory(view -> tipoCell());
        tipoCombo.setButtonCell(tipoCell());
    }

    private ListCell<Producto> productoCell() {
        return new ListCell<>() { @Override protected void updateItem(Producto item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? "" : item.nombre() + " · Stock " + item.stockActual()); } };
    }

    private ListCell<TipoMovimientoInventario> tipoCell() {
        return new ListCell<>() { @Override protected void updateItem(TipoMovimientoInventario item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? "" : item.label()); } };
    }

    private void cargarDatos() { listView.getItems().setAll(context.movimientoInventarioService().recientes(busquedaField.getText())); }

    private void registrar() {
        Producto producto = productoCombo.getValue();
        OperationResult<MovimientoInventario> result = context.movimientoInventarioService().registrarAjuste(
                producto == null ? null : producto.id(), tipoCombo.getValue(), decimalOrZero(cantidadField.getText()),
                motivoField.getText(), responsableField.getText(), observacionArea.getText()
        );
        if (result.success()) {
            AppDialog.info("Movimientos", "Movimiento registrado", result.message());
            limpiar();
            configurarCombos();
            cargarDatos();
        } else {
            AppDialog.warning("Movimientos", "No se pudo registrar", result.message());
        }
    }

    private void limpiar() {
        productoCombo.getSelectionModel().clearSelection();
        tipoCombo.getSelectionModel().clearSelection();
        cantidadField.setText("1");
        motivoField.clear();
        responsableField.clear();
        observacionArea.clear();
    }

    private BigDecimal decimalOrZero(String value) {
        if (value == null || value.isBlank()) return BigDecimal.ZERO;
        try { return new BigDecimal(value.strip().replace(',', '.')); } catch (NumberFormatException ex) { return BigDecimal.ZERO; }
    }
}
