package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.catalogos;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppCard;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.InfoPanel;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.ModuleScaffold;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.List;

/** Vista contenedora de los catálogos base del producto. */
public final class CatalogosBaseView {
    private final AppContext context;

    public CatalogosBaseView(AppContext context) {
        this.context = context;
    }

    public Node render() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(new Tab("Categorías", new CategoriaCatalogoPane(context.categoriaService()).render()));
        tabs.getTabs().add(new Tab("Marcas", new MarcaCatalogoPane(context.marcaService()).render()));
        tabs.getTabs().add(new Tab("Unidades", new UnidadMedidaCatalogoPane(context.unidadMedidaService()).render()));

        InfoPanel sidePanel = new InfoPanel(
                "Catálogos base",
                "Estos datos alimentan productos, compras, ventas internas y reportes.",
                "Regla de diseño: son transversales, pequeños y parametrizables. No deben depender de una tienda específica.",
                List.of(
                        "Categorías agrupan productos para búsqueda y reportes.",
                        "Marcas son opcionales, pero útiles para evitar nombres ambiguos.",
                        "Unidades definen si el stock usa enteros o decimales."
                )
        );
        sidePanel.setPrefWidth(330);

        return new ModuleScaffold(
                "Catálogos base",
                "Categorías, marcas y unidades de medida reutilizables para cualquier tienda local.",
                new AppCard(tabs),
                sidePanel,
                null
        );
    }
}
