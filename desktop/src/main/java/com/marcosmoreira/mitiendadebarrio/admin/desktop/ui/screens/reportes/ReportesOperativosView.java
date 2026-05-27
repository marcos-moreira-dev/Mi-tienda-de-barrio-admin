package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.reportes;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.reporte.ReporteOperativo;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.reporte.TipoReporteOperativo;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppCard;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppDialog;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.InfoPanel;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.ModuleScaffold;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.util.List;

/** Vista de reportes operativos locales. */
public final class ReportesOperativosView {
    private final AppContext context;
    private final ComboBox<TipoReporteOperativo> tipoCombo = new ComboBox<>();
    private final ListView<String> lineasView = new ListView<>();
    private final Label resumenLabel = new Label("Seleccione un reporte y presione Generar.");
    private ReporteOperativo reporteActual;

    public ReportesOperativosView(AppContext context) { this.context = context; }

    public Node render() {
        tipoCombo.getItems().setAll(TipoReporteOperativo.values());
        tipoCombo.setValue(TipoReporteOperativo.PRODUCTOS_POR_COMPRAR);
        tipoCombo.setCellFactory(view -> cell());
        tipoCombo.setButtonCell(cell());

        AppButton generarButton = AppButton.primary("Generar");
        generarButton.setOnAction(event -> generar());
        AppButton exportarButton = AppButton.secondary("Exportar CSV");
        exportarButton.setOnAction(event -> exportar());
        AppButton pdfButton = AppButton.secondary("Exportar PDF");
        pdfButton.setOnAction(event -> exportarPdf());

        VBox content = new VBox(10,
                new HBox(8, new Label("Reporte"), tipoCombo, generarButton, exportarButton, pdfButton),
                resumenLabel,
                lineasView
        );
        content.setPadding(new Insets(8));
        lineasView.setPrefHeight(420);
        Label placeholder = new Label("Genere un reporte para ver aquí los registros operativos.");
        placeholder.getStyleClass().add("muted-text");
        lineasView.setPlaceholder(placeholder);

        InfoPanel side = new InfoPanel(
                "Reportes operativos",
                "Pensados para comprar mejor, detectar stock bajo y revisar movimientos recientes.",
                "La tanda actual exporta CSV editable y PDF formal local para imprimir o entregar.",
                List.of("Productos por comprar usa stock objetivo.", "Los reportes no reemplazan contabilidad.", "Todo se genera localmente.")
        );
        side.setPrefWidth(330);

        generar();
        return new ModuleScaffold("Reportes", "Reportes locales para operación diaria y reposición.", new AppCard(content), side, null);
    }

    private ListCell<TipoReporteOperativo> cell() {
        return new ListCell<>() {
            @Override protected void updateItem(TipoReporteOperativo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.label());
            }
        };
    }

    private void generar() {
        reporteActual = context.reporteOperativoService().generar(tipoCombo.getValue());
        lineasView.getItems().setAll(reporteActual.lineas().stream().map(line -> line.asText()).toList());
        resumenLabel.setText(reporteActual.tipo().label() + " · " + reporteActual.lineas().size() + " registros · " + reporteActual.generadoEn());
    }

    private void exportar() {
        if (reporteActual == null) generar();
        OperationResult<Path> result = context.reporteOperativoService().exportarCsv(reporteActual);
        AppDialog.info("Exportación de reporte", "Reporte exportado", result.message());
    }

    private void exportarPdf() {
        if (reporteActual == null) generar();
        OperationResult<Path> result = context.reporteOperativoService().exportarPdfFormal(reporteActual);
        AppDialog.info("Exportación de reporte", "Reporte PDF generado", result.message());
    }
}

