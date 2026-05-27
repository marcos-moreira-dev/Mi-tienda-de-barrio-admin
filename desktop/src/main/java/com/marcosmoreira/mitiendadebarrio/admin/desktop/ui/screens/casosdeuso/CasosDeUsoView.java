package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.casosdeuso;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.casosdeuso.UseCaseCatalog;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.casosdeuso.UseCaseCatalogService;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppCard;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppListFactory;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppScrollFactory;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.InfoPanel;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.ModuleScaffold;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/** Hub operativo de casos de uso documentados. */
public final class CasosDeUsoView {

    private final UseCaseCatalogService useCaseCatalogService = new UseCaseCatalogService();
    private final List<UseCaseCatalog.UseCaseModule> modules = useCaseCatalogService.modules();
    private final List<AppButton> moduleButtons = new ArrayList<>();
    private final List<AppButton> caseButtons = new ArrayList<>();
    private final HBox caseButtonsBox = new HBox(10);
    private final Label caseHeaderTitle = new Label("Casos");
    private final Label caseHeaderSubtitle = new Label();
    private final VBox detailHost = new VBox(12);
    private UseCaseCatalog.UseCaseModule activeModule;
    private UseCaseCatalog.UseCaseItem activeCase;

    public Node render() {
        if (!modules.isEmpty()) {
            activeModule = modules.get(0);
            activeCase = useCaseCatalogService.firstCaseOf(activeModule).orElse(null);
        }

        VBox moduleSelector = buildModuleSelector();
        VBox caseSelector = buildCaseSelector();
        VBox detail = buildDetailPanel();

        VBox rightPane = new VBox(14, caseSelector, detail);
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        HBox hubBody = new HBox(18, moduleSelector, rightPane);
        hubBody.getStyleClass().add("use-case-hub-body");
        hubBody.setPadding(new Insets(2));

        VBox hub = new VBox(16,
                buildIntroCard(),
                new AppCard(hubBody)
        );
        VBox.setVgrow(hubBody, Priority.ALWAYS);

        InfoPanel side = new InfoPanel(
                "Guía de operación",
                "Los casos de uso están organizados por módulo, igual que una guía de capacitación breve: primero eliges el módulo, luego el caso puntual y finalmente revisas el paso a paso.",
                "La base sale de docs/07-casos-de-uso-generales.md y desktop/docs/manual-usuario/00_matriz_de_casos_de_uso.md, aterrizada a los módulos reales de la app.",
                List.of(
                        "Usar esta sección para capacitación o demo comercial.",
                        "Seleccionar módulo y luego caso concreto.",
                        "Ejecutar los pasos dentro de la app."
                )
        );
        side.setPrefWidth(360);

        refreshModules();
        refreshCases();
        refreshDetail();

        return new ModuleScaffold(
                "Casos de uso",
                "Hub operativo rápido con guías simples por módulo y por tarea.",
                hub,
                side,
                null
        );
    }

    private Node buildIntroCard() {
        Label title = new Label("Casos de uso");
        title.getStyleClass().add("use-case-intro-title");
        Label subtitle = new Label("Hub operativo rápido con guías simples por módulo y por tarea.");
        subtitle.getStyleClass().add("use-case-intro-subtitle");
        Label meta = new Label("Matriz con " + useCaseCatalogService.totalCases() + " casos de uso respaldados por documentación de alcance e implementación.");
        meta.getStyleClass().add("use-case-intro-meta");

        VBox texts = new VBox(5, title, subtitle, meta);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        AppButton help = AppButton.secondary("Ayuda del módulo");
        help.setDisable(true);

        HBox card = new HBox(16, texts, spacer, help);
        card.getStyleClass().add("use-case-intro-card");
        card.setAlignment(Pos.CENTER_LEFT);
        return card;
    }

    private VBox buildModuleSelector() {
        Label title = new Label("Módulos");
        title.getStyleClass().add("use-case-panel-title");
        Label subtitle = new Label("Elige el área del sistema donde quieres ver los casos disponibles.");
        subtitle.getStyleClass().add("use-case-panel-subtitle");
        subtitle.setWrapText(true);

        VBox buttons = new VBox(8);
        buttons.getStyleClass().add("use-case-module-list");
        for (UseCaseCatalog.UseCaseModule module : modules) {
            AppButton button = AppButton.secondary(module.name());
            button.getStyleClass().add("use-case-module-button");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(event -> {
                activeModule = module;
                activeCase = useCaseCatalogService.firstCaseOf(module).orElse(null);
                refreshModules();
                refreshCases();
                refreshDetail();
            });
            moduleButtons.add(button);
            buttons.getChildren().add(button);
        }

        Node scroll = AppScrollFactory.vertical(buttons, "use-case-module-scroll");

        VBox selector = new VBox(12, title, subtitle, scroll);
        selector.getStyleClass().add("use-case-module-panel");
        selector.setPrefWidth(270);
        selector.setMinWidth(245);
        return selector;
    }

    private VBox buildCaseSelector() {
        caseHeaderTitle.getStyleClass().add("use-case-panel-title");
        caseHeaderSubtitle.getStyleClass().add("use-case-panel-subtitle");
        caseHeaderSubtitle.setWrapText(true);

        caseButtonsBox.getStyleClass().add("use-case-chip-list");
        Node scroll = AppScrollFactory.horizontal(caseButtonsBox, "use-case-chip-scroll", 80);

        VBox selector = new VBox(8, caseHeaderTitle, caseHeaderSubtitle, scroll);
        selector.getStyleClass().add("use-case-case-panel");
        return selector;
    }

    private VBox buildDetailPanel() {
        detailHost.getStyleClass().add("use-case-detail-panel");
        VBox.setVgrow(detailHost, Priority.ALWAYS);
        return detailHost;
    }

    private void refreshModules() {
        for (int i = 0; i < moduleButtons.size(); i++) {
            AppButton button = moduleButtons.get(i);
            button.getStyleClass().remove("use-case-module-button-active");
            if (modules.get(i).equals(activeModule)) {
                button.getStyleClass().add("use-case-module-button-active");
            }
        }
    }

    private void refreshCases() {
        caseButtons.clear();
        caseButtonsBox.getChildren().clear();
        if (activeModule == null) {
            caseHeaderTitle.setText("Casos");
            caseHeaderSubtitle.setText("");
            return;
        }
        caseHeaderTitle.setText("Casos de " + activeModule.name());
        caseHeaderSubtitle.setText(activeModule.description() + " • " + activeModule.cases().size() + " casos disponibles.");
        for (UseCaseCatalog.UseCaseItem item : activeModule.cases()) {
            AppButton button = AppButton.secondary(item.code() + " · " + item.title());
            button.getStyleClass().add("use-case-chip-button");
            button.setMinWidth(240);
            button.setPrefWidth(260);
            button.setAlignment(Pos.CENTER);
            button.setOnAction(event -> {
                activeCase = item;
                refreshCases();
                refreshDetail();
            });
            if (item.equals(activeCase)) {
                button.getStyleClass().add("use-case-chip-button-active");
            }
            caseButtons.add(button);
            caseButtonsBox.getChildren().add(button);
        }
    }

    private void refreshDetail() {
        detailHost.getChildren().clear();
        if (activeCase == null) {
            Label empty = new Label("Seleccione un caso de uso para ver su guía operativa.");
            empty.getStyleClass().add("use-case-empty");
            detailHost.getChildren().add(empty);
            return;
        }

        Label module = new Label(activeCase.module());
        module.getStyleClass().add("use-case-detail-module");
        Label code = new Label(activeCase.code());
        code.getStyleClass().add("use-case-detail-code");
        HBox meta = new HBox(8, module, code);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(activeCase.title());
        title.getStyleClass().add("use-case-detail-title");
        title.setWrapText(true);

        detailHost.getChildren().addAll(meta, title, separator(), section("Qué permite hacer", activeCase.purpose()), section("Desde dónde inicia", activeCase.start()), stepsSection(activeCase.steps()), section("Resultado esperado", activeCase.result()));
    }

    private Node section(String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("use-case-section-title");
        Label bodyLabel = new Label(body);
        bodyLabel.getStyleClass().add("use-case-section-body");
        bodyLabel.setWrapText(true);
        return new VBox(5, titleLabel, bodyLabel);
    }

    private Node stepsSection(List<String> steps) {
        Label title = new Label("Pasos exactos");
        title.getStyleClass().add("use-case-section-title");
        VBox rows = AppListFactory.numberedSteps(
                steps,
                "use-case-step-row",
                "use-case-step-number",
                "use-case-step-text"
        );
        return new VBox(8, title, rows);
    }

    private Node separator() {
        Region line = new Region();
        line.getStyleClass().add("use-case-separator");
        line.setMinHeight(1);
        line.setPrefHeight(1);
        return line;
    }
}
