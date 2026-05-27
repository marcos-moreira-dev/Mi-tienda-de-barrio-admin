package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.proveedores;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.proveedor.ProveedorService;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.proveedor.EstadoProveedor;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.proveedor.Proveedor;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppCard;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppDialog;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.InfoPanel;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.ModuleScaffold;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/** Vista del módulo Proveedores. */
public final class ProveedoresView {
    private final ProveedorService service;
    private final ListView<Proveedor> listView = new ListView<>();
    private final TextField nombreField = new TextField();
    private final TextField telefonoField = new TextField();
    private final TextField whatsappField = new TextField();
    private final TextField direccionField = new TextField();
    private final TextArea observacionArea = new TextArea();
    private final CheckBox incluirInactivosCheck = new CheckBox("Mostrar inactivos");
    private Proveedor seleccionado;

    public ProveedoresView(AppContext context) {
        this.service = context.proveedorService();
    }

    public Node render() {
        configurarLista();
        cargarDatos();

        nombreField.setPromptText("Ej. Distribuidora local, proveedor de yogur, mercado mayorista");
        telefonoField.setPromptText("Teléfono fijo o celular");
        whatsappField.setPromptText("WhatsApp si aplica");
        direccionField.setPromptText("Dirección o referencia");
        observacionArea.setPromptText("Condiciones, frecuencia de visita, productos que trae, incidentes.");
        observacionArea.setPrefRowCount(4);
        observacionArea.setWrapText(true);

        AppButton nuevoButton = AppButton.ghost("Nuevo");
        nuevoButton.setOnAction(event -> limpiarFormulario());

        AppButton guardarButton = AppButton.primary("Guardar proveedor");
        guardarButton.setOnAction(event -> guardar());

        AppButton cambiarEstadoButton = AppButton.secondary("Activar / desactivar");
        cambiarEstadoButton.setOnAction(event -> cambiarEstado());

        incluirInactivosCheck.setOnAction(event -> cargarDatos());

        VBox form = new VBox(8,
                new Label("Nombre *"), nombreField,
                new Label("Teléfono"), telefonoField,
                new Label("WhatsApp"), whatsappField,
                new Label("Dirección"), direccionField,
                new Label("Observación"), observacionArea,
                new HBox(8, nuevoButton, guardarButton, cambiarEstadoButton)
        );
        form.setPadding(new Insets(8));
        HBox.setHgrow(form, Priority.ALWAYS);

        VBox left = new VBox(8, incluirInactivosCheck, listView);
        left.setPadding(new Insets(8));
        left.setPrefWidth(340);
        VBox.setVgrow(listView, Priority.ALWAYS);

        InfoPanel sidePanel = new InfoPanel(
                "Proveedores",
                "Registra fuentes de abastecimiento sin convertir el sistema en contabilidad.",
                "Este módulo prepara compras, historial de costos y reportes por proveedor.",
                List.of(
                        "Nombre obligatorio; teléfono y WhatsApp son opcionales.",
                        "La observación permite guardar acuerdos informales o incidencias.",
                        "Desactivar conserva trazabilidad sin borrar datos."
                )
        );
        sidePanel.setPrefWidth(330);

        return new ModuleScaffold(
                "Proveedores",
                "Contactos y fuentes de mercadería para compras, reposición y trazabilidad comercial.",
                new AppCard(new HBox(12, left, form)),
                sidePanel,
                null
        );
    }

    private void configurarLista() {
        listView.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.nombre() + " · " + item.estado().label());
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, item) -> seleccionar(item));
    }

    private void cargarDatos() {
        listView.getItems().setAll(service.listar(incluirInactivosCheck.isSelected()));
    }

    private void seleccionar(Proveedor item) {
        seleccionado = item;
        if (item == null) { return; }
        nombreField.setText(item.nombre());
        telefonoField.setText(item.telefono());
        whatsappField.setText(item.whatsapp());
        direccionField.setText(item.direccion());
        observacionArea.setText(item.observacion());
    }

    private void limpiarFormulario() {
        seleccionado = null;
        listView.getSelectionModel().clearSelection();
        nombreField.clear();
        telefonoField.clear();
        whatsappField.clear();
        direccionField.clear();
        observacionArea.clear();
    }

    private void guardar() {
        Proveedor proveedor = new Proveedor(
                seleccionado == null ? null : seleccionado.id(),
                nombreField.getText(),
                telefonoField.getText(),
                whatsappField.getText(),
                direccionField.getText(),
                observacionArea.getText(),
                seleccionado == null ? EstadoProveedor.ACTIVO : seleccionado.estado()
        );
        OperationResult<Proveedor> result = service.guardar(proveedor);
        if (result.success()) {
            AppDialog.info("Proveedores", "Cambios guardados", result.message());
            cargarDatos();
            result.data().ifPresent(this::seleccionar);
        } else {
            AppDialog.warning("Proveedores", "No se pudo guardar", result.message());
        }
    }

    private void cambiarEstado() {
        if (seleccionado == null || seleccionado.id() == null) {
            AppDialog.warning("Proveedores", "Seleccione un proveedor", "Debe seleccionar un proveedor existente antes de cambiar su estado.");
            return;
        }
        OperationResult<Void> result = seleccionado.estado() == EstadoProveedor.ACTIVO
                ? service.desactivar(seleccionado.id())
                : service.reactivar(seleccionado.id());
        AppDialog.info("Proveedores", "Estado actualizado", result.message());
        cargarDatos();
        limpiarFormulario();
    }
}
