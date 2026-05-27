package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.ayuda;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.ayuda.AyudaContextual;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;
import java.util.stream.Collectors;

/** Mini manual interno y ayuda contextual por módulo. */
public final class AyudaContextualView {
    private final AppContext context;
    private final ListView<AyudaContextual> listView = new ListView<>();
    private final TextArea contenidoArea = new TextArea();

    public AyudaContextualView(AppContext context) { this.context = context; }

    public Node render() {
        context.ayudaContextualService().asegurarContenidoBase();
        listView.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(AyudaContextual item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.modulo() + " · " + item.titulo());
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, item) -> {
            contenidoArea.setText(item == null ? "" : item.contenido());
        });
        listView.getItems().setAll(context.ayudaContextualService().todas());
        contenidoArea.setWrapText(true);
        contenidoArea.setEditable(false);
        VBox content = new VBox(10, new Label("Temas de ayuda"), listView, new Label("Contenido"), contenidoArea);
        content.setPadding(new Insets(8));
        VBox.setVgrow(listView, Priority.ALWAYS);
        VBox.setVgrow(contenidoArea, Priority.ALWAYS);
        List<String> modulos = context.ayudaContextualService().todas().stream().map(AyudaContextual::modulo).distinct().collect(Collectors.toList());
        InfoPanel side = new InfoPanel("Ayuda contextual", "Mini manual integrado en la aplicación.", "Pensado para reducir dependencia del desarrollador en tareas diarias.", modulos);
        side.setPrefWidth(330);
        return new ModuleScaffold("Ayuda", "Manual corto por módulo y ruta diaria recomendada.", new AppCard(content), side, null);
    }
}
