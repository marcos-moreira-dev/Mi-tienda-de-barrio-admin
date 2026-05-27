package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.compras;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.Compra;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.RegistroCompraSimple;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.TipoComprobanteCompra;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.Producto;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.proveedor.Proveedor;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.*;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Vista de compras y entradas de mercadería. */
public final class ComprasEntradasView {
    private final AppContext context;
    private final ListView<Compra> listView = new ListView<>();
    private final TextField busquedaField = new TextField();
    private final ComboBox<Proveedor> proveedorCombo = new ComboBox<>();
    private final ComboBox<Producto> productoCombo = new ComboBox<>();
    private final TextField cantidadField = new TextField("1");
    private final TextField costoField = new TextField("0");
    private final DatePicker fechaCompraPicker = new DatePicker(LocalDate.now());
    private final ComboBox<TipoComprobanteCompra> tipoComprobanteCombo = new ComboBox<>();
    private final TextField numeroComprobanteField = new TextField();
    private final TextField codigoLoteField = new TextField();
    private final DatePicker fechaVencimientoPicker = new DatePicker();
    private final TextArea observacionArea = new TextArea();

    public ComprasEntradasView(AppContext context) { this.context = context; }

    public Node render() {
        configurarLista();
        configurarCombos();
        cargarDatos();

        busquedaField.setPromptText("Buscar por proveedor, comprobante u observación");
        numeroComprobanteField.setPromptText("Número de nota/factura si existe");
        codigoLoteField.setPromptText("Lote opcional");
        observacionArea.setPromptText("Observación de recepción: estado, proveedor, incidencia, etc.");
        observacionArea.setPrefRowCount(3);
        observacionArea.setWrapText(true);

        AppButton buscarButton = AppButton.secondary("Buscar");
        buscarButton.setOnAction(event -> cargarDatos());
        AppButton registrarButton = AppButton.primary("Registrar entrada");
        registrarButton.setOnAction(event -> registrar());
        AppButton limpiarButton = AppButton.ghost("Limpiar");
        limpiarButton.setOnAction(event -> limpiar());

        Node listado = new ComprasListPane(busquedaField, buscarButton, listView);
        Node formulario = new ComprasFormPane(
                proveedorCombo, productoCombo, cantidadField, costoField, fechaCompraPicker, tipoComprobanteCombo,
                numeroComprobanteField, codigoLoteField, fechaVencimientoPicker, observacionArea, limpiarButton, registrarButton
        );

        InfoPanel info = new InfoPanel(
                "Recepción de mercadería",
                "Registrar una compra aumenta stock, guarda detalle y crea movimiento ENTRADA_COMPRA.",
                "Si el producto maneja lote o vencimiento, la entrada puede crear un lote local para trazabilidad.",
                List.of("No reemplaza contabilidad ni SRI.", "Costo unitario actualiza precio de compra referencial.", "Usar observación para problemas de proveedor o envase.")
        );
        info.setPrefWidth(330);

        return new ModuleScaffold(
                "Compras / Entradas",
                "Recepción simple de mercadería con actualización automática de stock.",
                new AppCard(new HBox(12, listado, formulario)),
                info,
                null
        );
    }

    private void configurarLista() {
        listView.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(Compra item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText("Compra #" + item.id() + " · " + (item.proveedorNombre() == null ? "Sin proveedor" : item.proveedorNombre())
                        + " · " + item.fechaCompra() + " · Total " + item.totalEstimado());
            }
        });
    }

    private void configurarCombos() {
        proveedorCombo.getItems().setAll(context.proveedorService().listar(false));
        productoCombo.getItems().setAll(context.productoService().listar("", false));
        tipoComprobanteCombo.getItems().setAll(TipoComprobanteCompra.values());
        tipoComprobanteCombo.setValue(TipoComprobanteCompra.SIN_COMPROBANTE);
        proveedorCombo.setCellFactory(view -> proveedorCell()); proveedorCombo.setButtonCell(proveedorCell());
        productoCombo.setCellFactory(view -> productoCell()); productoCombo.setButtonCell(productoCell());
        tipoComprobanteCombo.setCellFactory(view -> tipoCell()); tipoComprobanteCombo.setButtonCell(tipoCell());
    }

    private ListCell<Proveedor> proveedorCell() { return new ListCell<>() { @Override protected void updateItem(Proveedor item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? "" : item.nombre()); } }; }
    private ListCell<Producto> productoCell() { return new ListCell<>() { @Override protected void updateItem(Producto item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? "" : item.nombre() + " · Stock " + item.stockActual()); } }; }
    private ListCell<TipoComprobanteCompra> tipoCell() { return new ListCell<>() { @Override protected void updateItem(TipoComprobanteCompra item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? "" : item.label()); } }; }
    private void cargarDatos() { listView.getItems().setAll(context.compraService().recientes(busquedaField.getText())); }

    private void registrar() {
        Proveedor proveedor = proveedorCombo.getValue();
        Producto producto = productoCombo.getValue();
        RegistroCompraSimple command = new RegistroCompraSimple(
                proveedor == null ? null : proveedor.id(), producto == null ? null : producto.id(), decimalOrZero(cantidadField.getText()),
                decimalOrZero(costoField.getText()), fechaCompraPicker.getValue(), tipoComprobanteCombo.getValue(), numeroComprobanteField.getText(),
                codigoLoteField.getText(), fechaVencimientoPicker.getValue(), observacionArea.getText()
        );
        OperationResult<Compra> result = context.compraService().registrar(command);
        if (result.success()) {
            AppDialog.info("Compras", "Entrada registrada", result.message());
            limpiar(); configurarCombos(); cargarDatos();
        } else {
            AppDialog.warning("Compras", "No se pudo registrar", result.message());
        }
    }

    private void limpiar() {
        proveedorCombo.getSelectionModel().clearSelection(); productoCombo.getSelectionModel().clearSelection();
        cantidadField.setText("1"); costoField.setText("0"); fechaCompraPicker.setValue(LocalDate.now());
        tipoComprobanteCombo.setValue(TipoComprobanteCompra.SIN_COMPROBANTE); numeroComprobanteField.clear(); codigoLoteField.clear();
        fechaVencimientoPicker.setValue(null); observacionArea.clear();
    }

    private BigDecimal decimalOrZero(String value) {
        if (value == null || value.isBlank()) return BigDecimal.ZERO;
        try { return new BigDecimal(value.strip().replace(',', '.')); } catch (NumberFormatException ex) { return BigDecimal.ZERO; }
    }
}
