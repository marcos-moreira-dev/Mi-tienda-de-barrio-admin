package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.catalogos;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.catalogo.UnidadMedidaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.EstadoCatalogo;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.UnidadMedida;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppDialog;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Panel CRUD local para unidades de medida. */
final class UnidadMedidaCatalogoPane {
    private final UnidadMedidaService service;
    private final ListView<UnidadMedida> listView = new ListView<>();
    private final TextField nombreField = new TextField();
    private final TextField abreviaturaField = new TextField();
    private final CheckBox permiteDecimalesCheck = new CheckBox("Permite decimales");
    private final CheckBox incluirInactivasCheck = new CheckBox("Mostrar inactivas");
    private UnidadMedida seleccionada;

    UnidadMedidaCatalogoPane(UnidadMedidaService service) {
        this.service = service;
    }

    Node render() {
        configurarLista();
        cargarDatos();

        nombreField.setPromptText("Ej. Unidad, Libra, Kilogramo, Caja");
        abreviaturaField.setPromptText("Ej. u, lb, kg, caja");

        AppButton nuevoButton = AppButton.ghost("Nuevo");
        nuevoButton.setOnAction(event -> limpiarFormulario());

        AppButton guardarButton = AppButton.primary("Guardar unidad");
        guardarButton.setOnAction(event -> guardar());

        AppButton cambiarEstadoButton = AppButton.secondary("Activar / desactivar");
        cambiarEstadoButton.setOnAction(event -> cambiarEstado());

        incluirInactivasCheck.setOnAction(event -> cargarDatos());

        VBox form = new VBox(8,
                new Label("Nombre *"), nombreField,
                new Label("Abreviatura *"), abreviaturaField,
                permiteDecimalesCheck,
                new HBox(8, nuevoButton, guardarButton, cambiarEstadoButton)
        );
        form.setPadding(new Insets(8));
        HBox.setHgrow(form, Priority.ALWAYS);

        VBox left = new VBox(8, incluirInactivasCheck, listView);
        left.setPadding(new Insets(8));
        left.setPrefWidth(330);
        VBox.setVgrow(listView, Priority.ALWAYS);

        HBox root = new HBox(12, left, form);
        root.setPadding(new Insets(8));
        return root;
    }

    private void configurarLista() {
        listView.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(UnidadMedida item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.nombre() + " (" + item.abreviatura() + ") · "
                            + item.permiteDecimalesTexto() + " decimales · " + item.estado().label());
                }
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, item) -> seleccionar(item));
    }

    private void cargarDatos() {
        listView.getItems().setAll(service.listar(incluirInactivasCheck.isSelected()));
    }

    private void seleccionar(UnidadMedida item) {
        seleccionada = item;
        if (item == null) {
            return;
        }
        nombreField.setText(item.nombre());
        abreviaturaField.setText(item.abreviatura());
        permiteDecimalesCheck.setSelected(item.permiteDecimales());
    }

    private void limpiarFormulario() {
        seleccionada = null;
        listView.getSelectionModel().clearSelection();
        nombreField.clear();
        abreviaturaField.clear();
        permiteDecimalesCheck.setSelected(false);
    }

    private void guardar() {
        UnidadMedida unidadMedida = new UnidadMedida(
                seleccionada == null ? null : seleccionada.id(),
                nombreField.getText(),
                abreviaturaField.getText(),
                permiteDecimalesCheck.isSelected(),
                seleccionada == null ? EstadoCatalogo.ACTIVA : seleccionada.estado()
        );
        OperationResult<UnidadMedida> result = service.guardar(unidadMedida);
        if (result.success()) {
            AppDialog.info("Unidades", "Cambios guardados", result.message());
            cargarDatos();
            result.data().ifPresent(this::seleccionar);
        } else {
            AppDialog.warning("Unidades", "No se pudo guardar", result.message());
        }
    }

    private void cambiarEstado() {
        if (seleccionada == null || seleccionada.id() == null) {
            AppDialog.warning("Unidades", "Seleccione una unidad", "Debe seleccionar una unidad existente antes de cambiar su estado.");
            return;
        }
        OperationResult<Void> result = seleccionada.estado() == EstadoCatalogo.ACTIVA
                ? service.desactivar(seleccionada.id())
                : service.reactivar(seleccionada.id());
        AppDialog.info("Unidades", "Estado actualizado", result.message());
        cargarDatos();
        limpiarFormulario();
    }
}
