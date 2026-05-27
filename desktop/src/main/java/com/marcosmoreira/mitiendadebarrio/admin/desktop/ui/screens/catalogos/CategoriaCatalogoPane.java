package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.catalogos;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.catalogo.CategoriaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.Categoria;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.EstadoCatalogo;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppDialog;
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

/** Panel CRUD local para categorías. */
final class CategoriaCatalogoPane {
    private final CategoriaService service;
    private final ListView<Categoria> listView = new ListView<>();
    private final TextField nombreField = new TextField();
    private final TextArea descripcionArea = new TextArea();
    private final CheckBox incluirInactivasCheck = new CheckBox("Mostrar inactivas");
    private Categoria seleccionada;

    CategoriaCatalogoPane(CategoriaService service) {
        this.service = service;
    }

    Node render() {
        configurarLista();
        cargarDatos();

        nombreField.setPromptText("Ej. Bebidas, Lácteos, Limpieza");
        descripcionArea.setPromptText("Uso interno de esta categoría.");
        descripcionArea.setPrefRowCount(3);
        descripcionArea.setWrapText(true);

        AppButton nuevoButton = AppButton.ghost("Nuevo");
        nuevoButton.setOnAction(event -> limpiarFormulario());

        AppButton guardarButton = AppButton.primary("Guardar categoría");
        guardarButton.setOnAction(event -> guardar());

        AppButton cambiarEstadoButton = AppButton.secondary("Activar / desactivar");
        cambiarEstadoButton.setOnAction(event -> cambiarEstado());

        incluirInactivasCheck.setOnAction(event -> cargarDatos());

        VBox form = new VBox(8,
                new Label("Nombre *"), nombreField,
                new Label("Descripción"), descripcionArea,
                new HBox(8, nuevoButton, guardarButton, cambiarEstadoButton)
        );
        form.setPadding(new Insets(8));
        HBox.setHgrow(form, Priority.ALWAYS);

        VBox left = new VBox(8, incluirInactivasCheck, listView);
        left.setPadding(new Insets(8));
        left.setPrefWidth(310);
        VBox.setVgrow(listView, Priority.ALWAYS);

        HBox root = new HBox(12, left, form);
        root.setPadding(new Insets(8));
        return root;
    }

    private void configurarLista() {
        listView.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.nombre() + " · " + item.estado().label());
                }
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, item) -> seleccionar(item));
    }

    private void cargarDatos() {
        listView.getItems().setAll(service.listar(incluirInactivasCheck.isSelected()));
    }

    private void seleccionar(Categoria item) {
        seleccionada = item;
        if (item == null) {
            return;
        }
        nombreField.setText(item.nombre());
        descripcionArea.setText(item.descripcion());
    }

    private void limpiarFormulario() {
        seleccionada = null;
        listView.getSelectionModel().clearSelection();
        nombreField.clear();
        descripcionArea.clear();
    }

    private void guardar() {
        Categoria categoria = new Categoria(
                seleccionada == null ? null : seleccionada.id(),
                nombreField.getText(),
                descripcionArea.getText(),
                seleccionada == null ? EstadoCatalogo.ACTIVA : seleccionada.estado()
        );
        OperationResult<Categoria> result = service.guardar(categoria);
        if (result.success()) {
            AppDialog.info("Categorías", "Cambios guardados", result.message());
            cargarDatos();
            result.data().ifPresent(this::seleccionar);
        } else {
            AppDialog.warning("Categorías", "No se pudo guardar", result.message());
        }
    }

    private void cambiarEstado() {
        if (seleccionada == null || seleccionada.id() == null) {
            AppDialog.warning("Categorías", "Seleccione una categoría", "Debe seleccionar una categoría existente antes de cambiar su estado.");
            return;
        }
        OperationResult<Void> result = seleccionada.estado() == EstadoCatalogo.ACTIVA
                ? service.desactivar(seleccionada.id())
                : service.reactivar(seleccionada.id());
        AppDialog.info("Categorías", "Estado actualizado", result.message());
        cargarDatos();
        limpiarFormulario();
    }
}
