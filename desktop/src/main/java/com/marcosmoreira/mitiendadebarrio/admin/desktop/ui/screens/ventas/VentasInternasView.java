package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.ventas;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.Producto;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.MetodoPagoVentaInterna;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.RegistroVentaInternaSimple;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.VentaInterna;
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

/** Vista de ventas internas y salidas operativas de stock. */
public final class VentasInternasView {
    private final AppContext context;
    private final ListView<VentaInterna> listView = new ListView<>();
    private final TextField busquedaField = new TextField();
    private final ComboBox<Producto> productoCombo = new ComboBox<>();
    private final TextField cantidadField = new TextField("1");
    private final TextField precioField = new TextField("0");
    private final ComboBox<MetodoPagoVentaInterna> metodoPagoCombo = new ComboBox<>();
    private final TextField referenciaField = new TextField();
    private final CheckBox advertenciaCheck = new CheckBox("Entiendo que esto es control interno y no reemplaza facturación ni comprobantes oficiales.");
    private final TextArea observacionArea = new TextArea();

    public VentasInternasView(AppContext context) { this.context = context; }

    public Node render() {
        configurarLista();
        configurarCombos();
        cargarDatos();

        busquedaField.setPromptText("Buscar por referencia, método u observación");
        referenciaField.setPromptText("Referencia opcional: nota externa, transferencia, etc.");
        observacionArea.setPromptText("Observación interna de la salida.");
        observacionArea.setPrefRowCount(3);
        observacionArea.setWrapText(true);

        AppButton buscarButton = AppButton.secondary("Buscar");
        buscarButton.setOnAction(event -> cargarDatos());
        AppButton registrarButton = AppButton.primary("Registrar salida");
        registrarButton.setOnAction(event -> registrar());
        AppButton limpiarButton = AppButton.ghost("Limpiar");
        limpiarButton.setOnAction(event -> limpiar());

        Node listado = new VentasListPane(busquedaField, buscarButton, listView);
        Node formulario = new VentasFormPane(
                productoCombo, cantidadField, precioField, metodoPagoCombo, referenciaField,
                advertenciaCheck, observacionArea, limpiarButton, registrarButton
        );

        InfoPanel info = new InfoPanel(
                "Venta interna no tributaria",
                "Este flujo descuenta stock y crea movimiento SALIDA_VENTA_INTERNA.",
                "No reemplaza facturación electrónica, notas de venta ni obligaciones tributarias del negocio.",
                List.of("No permite stock negativo.", "El total se calcula cantidad × precio unitario.", "Sirve como control operativo local.")
        );
        info.setPrefWidth(330);

        return new ModuleScaffold(
                "Salidas / Ventas internas",
                "Descargo operativo de stock para control interno del negocio.",
                new AppCard(new HBox(12, listado, formulario)),
                info,
                null
        );
    }

    private void configurarLista() {
        listView.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(VentaInterna item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText("Venta #" + item.id() + " · " + item.fechaVenta() + " · " + item.metodoPago().label() + " · Total " + item.total());
            }
        });
    }

    private void configurarCombos() {
        productoCombo.getItems().setAll(context.productoService().listar("", false));
        productoCombo.setCellFactory(view -> productoCell());
        productoCombo.setButtonCell(productoCell());
        metodoPagoCombo.getItems().setAll(MetodoPagoVentaInterna.values());
        metodoPagoCombo.setValue(MetodoPagoVentaInterna.EFECTIVO);
        metodoPagoCombo.setCellFactory(view -> metodoCell());
        metodoPagoCombo.setButtonCell(metodoCell());
    }

    private ListCell<Producto> productoCell() {
        return new ListCell<>() {
            @Override protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.nombre() + " · Stock " + item.stockActual() + " · PVP " + item.precioVenta());
            }
        };
    }

    private ListCell<MetodoPagoVentaInterna> metodoCell() {
        return new ListCell<>() { @Override protected void updateItem(MetodoPagoVentaInterna item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? "" : item.label()); } };
    }

    private void cargarDatos() { listView.getItems().setAll(context.ventaInternaService().recientes(busquedaField.getText())); }

    private void registrar() {
        Producto producto = productoCombo.getValue();
        BigDecimal precio = precioField.getText() == null || precioField.getText().isBlank()
                ? producto == null ? BigDecimal.ZERO : producto.precioVenta()
                : decimalOrZero(precioField.getText());
        RegistroVentaInternaSimple command = new RegistroVentaInternaSimple(
                producto == null ? null : producto.id(), decimalOrZero(cantidadField.getText()), precio,
                metodoPagoCombo.getValue(), referenciaField.getText(), advertenciaCheck.isSelected(), observacionArea.getText()
        );
        OperationResult<VentaInterna> result = context.ventaInternaService().registrar(command);
        if (result.success()) {
            AppDialog.info("Ventas internas", "Salida registrada", result.message());
            limpiar(); configurarCombos(); cargarDatos();
        } else {
            AppDialog.warning("Ventas internas", "No se pudo registrar", result.message());
        }
    }

    private void limpiar() {
        productoCombo.getSelectionModel().clearSelection();
        cantidadField.setText("1");
        precioField.setText("0");
        metodoPagoCombo.setValue(MetodoPagoVentaInterna.EFECTIVO);
        referenciaField.clear();
        advertenciaCheck.setSelected(false);
        observacionArea.clear();
    }

    private BigDecimal decimalOrZero(String value) {
        if (value == null || value.isBlank()) return BigDecimal.ZERO;
        try { return new BigDecimal(value.strip().replace(',', '.')); } catch (NumberFormatException ex) { return BigDecimal.ZERO; }
    }
}
