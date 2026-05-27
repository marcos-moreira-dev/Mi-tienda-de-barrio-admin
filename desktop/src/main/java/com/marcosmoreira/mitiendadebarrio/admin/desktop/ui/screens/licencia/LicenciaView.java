package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.licencia;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.license.LicenseInfo;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppCard;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppDialog;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.InfoPanel;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.ModuleScaffold;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

/** Vista de licencia local renovable. */
public final class LicenciaView {
    private final AppContext context;
    private final Label estadoLabel = new Label();
    private final TextField codigoField = new TextField();
    private final DatePicker vencimientoPicker = new DatePicker(LocalDate.now().plusMonths(3));
    private final TextArea observacionArea = new TextArea();

    public LicenciaView(AppContext context) { this.context = context; }

    public Node render() {
        codigoField.setPromptText("Código de activación entregado al cliente");
        observacionArea.setPromptText("Observación comercial o soporte asociado a esta renovación.");
        observacionArea.setPrefRowCount(3);
        cargarEstado();

        AppButton activarButton = AppButton.primary("Activar / renovar");
        activarButton.setOnAction(event -> activar());
        AppButton refrescarButton = AppButton.secondary("Refrescar estado");
        refrescarButton.setOnAction(event -> cargarEstado());

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.add(new Label("Estado actual"), 0, 0); form.add(estadoLabel, 1, 0);
        form.add(new Label("Código"), 0, 1); form.add(codigoField, 1, 1);
        form.add(new Label("Vence"), 0, 2); form.add(vencimientoPicker, 1, 2);
        form.add(new Label("Observación"), 0, 3); form.add(observacionArea, 1, 3);

        VBox content = new VBox(10, form, new HBox(8, activarButton, refrescarButton));
        content.setPadding(new Insets(8));

        InfoPanel side = new InfoPanel("Licencia ética", "Controla renovaciones sin secuestrar los datos del cliente.",
                "Si vence, la estrategia recomendada es modo limitado: consulta, respaldo y exportación siguen disponibles.",
                List.of("No borrar información.", "Avisar antes de vencer.", "El soporte se conversa por periodo."));
        side.setPrefWidth(330);

        return new ModuleScaffold("Licencia", "Activación local y estado de uso del sistema.", new AppCard(content), side, null);
    }

    private void cargarEstado() {
        LicenseInfo info = context.licenseService().info();
        estadoLabel.setText(info.resumen());
        codigoField.setText(info.codigo());
        vencimientoPicker.setValue(info.fechaVencimiento() == null ? LocalDate.now().plusMonths(3) : info.fechaVencimiento());
        observacionArea.setText(info.observacion());
    }

    private void activar() {
        OperationResult<LicenseInfo> result = context.licenseService().activar(codigoField.getText(), vencimientoPicker.getValue(), observacionArea.getText());
        AppDialog.info("Licencia", "Licencia actualizada", result.message());
        cargarEstado();
    }
}
