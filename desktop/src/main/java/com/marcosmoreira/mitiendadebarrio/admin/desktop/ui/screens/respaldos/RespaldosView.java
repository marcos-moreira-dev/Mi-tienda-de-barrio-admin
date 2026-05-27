package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.respaldos;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.respaldo.RespaldoSistema;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppCard;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppDialog;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.InfoPanel;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.ModuleScaffold;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.io.IOException;
import java.util.List;

/** Vista de respaldos y restauración guiada. */
public final class RespaldosView {
    private final AppContext context;
    private final ListView<RespaldoSistema> respaldosView = new ListView<>();
    private final TextArea observacionArea = new TextArea();

    public RespaldosView(AppContext context) { this.context = context; }

    public Node render() {
        observacionArea.setPromptText("Observación opcional del respaldo.");
        observacionArea.setPrefRowCount(2);
        respaldosView.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(RespaldoSistema item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.resumen());
            }
        });
        cargar();

        AppButton crearButton = AppButton.primary("Crear respaldo");
        crearButton.setOnAction(event -> crear());
        AppButton refrescarButton = AppButton.secondary("Refrescar");
        refrescarButton.setOnAction(event -> cargar());
        AppButton carpetaButton = AppButton.secondary("Abrir carpeta");
        carpetaButton.setOnAction(event -> abrirCarpeta());
        AppButton restaurarButton = AppButton.ghost("Restaurar seleccionado");
        restaurarButton.setOnAction(event -> restaurarSeleccionado());

        VBox content = new VBox(10, observacionArea, new HBox(8, crearButton, refrescarButton, carpetaButton, restaurarButton), respaldosView);
        content.setPadding(new Insets(8));
        respaldosView.setPrefHeight(420);

        InfoPanel side = new InfoPanel(
                "Respaldos locales",
                "Protegen la base SQLite ante apagones, daño de equipo o cambios delicados.",
                "Restaurar reemplaza la base actual. El sistema crea un respaldo previo antes de hacerlo.",
                List.of("Guardar copias en pendrive o disco externo.", "No borrar la carpeta de datos.", "Cerrar y abrir la app después de restaurar.")
        );
        side.setPrefWidth(330);

        return new ModuleScaffold("Respaldos", "Crear, listar y restaurar copias locales de seguridad.", new AppCard(content), side, null);
    }

    private void cargar() { respaldosView.getItems().setAll(context.respaldoService().listarRecientes()); }

    private void crear() {
        OperationResult<RespaldoSistema> result = context.respaldoService().crearManual(observacionArea.getText());
        AppDialog.info("Respaldo", "Operación de respaldo", result.message());
        cargar();
    }

    private void restaurarSeleccionado() {
        RespaldoSistema selected = respaldosView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AppDialog.info("Restauración", "Seleccione un respaldo", "Seleccione un respaldo de la lista.");
            return;
        }
        OperationResult<Void> result = context.respaldoService().restaurar(selected.rutaArchivo());
        AppDialog.info("Restauración", "Resultado de restauración", result.message());
    }

    private void abrirCarpeta() {
        try {
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(context.paths().backupsDirectory().toFile());
        } catch (IOException ex) {
            AppDialog.info("Carpeta de respaldos", "No se pudo abrir la carpeta", "Ruta: " + context.paths().backupsDirectory());
        }
    }
}
