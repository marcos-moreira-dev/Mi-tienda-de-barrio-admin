package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.dashboard;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.dashboard.DashboardResumen;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppCard;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.InfoPanel;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.ModuleScaffold;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.StatusBadge;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Pantalla de inicio con resumen operativo real. */
public final class DashboardView {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final AppContext context;

    public DashboardView(AppContext context) { this.context = context; }

    public Node render() {
        DashboardResumen resumen = context.dashboardService().resumen();
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(8));

        grid.add(metric("Productos activos", String.valueOf(resumen.productosActivos()), "Catálogo disponible para operación."), 0, 0);
        grid.add(metric("Bajo stock", String.valueOf(resumen.productosBajoStock()), "Productos activos por debajo o en mínimo."), 1, 0);
        grid.add(metric("Agotados", String.valueOf(resumen.productosAgotados()), "Productos con stock en cero."), 2, 0);
        grid.add(metric("Por comprar", String.valueOf(resumen.productosPorComprar()), "Base del reporte de reposición."), 0, 1);
        grid.add(metric("Próximos a vencer", String.valueOf(resumen.productosProximosVencer()), "Lotes disponibles que vencen en 30 días."), 1, 1);
        grid.add(metric("Ventas internas hoy", money(resumen.ventasHoySeguro()), "Control interno, no tributario."), 2, 1);
        grid.add(metric("Compras hoy", money(resumen.comprasHoySeguro()), "Entradas registradas hoy."), 0, 2);
        grid.add(metric("Caja", resumen.cajaAbierta() ? "Abierta" : "Sin abrir", "Estado de caja del día."), 1, 2);
        grid.add(metric("Último respaldo", resumen.ultimoRespaldo() == null ? "Sin registro" : DATE_TIME.format(resumen.ultimoRespaldo()), "Recordatorio operativo."), 2, 2);

        VBox content = new VBox(12, new AppCard(grid), new StatusBadge("Licencia: " + resumen.estadoLicencia()));
        InfoPanel side = new InfoPanel(
                "Inicio operativo",
                "Resumen calculado desde SQLite para que el dueño vea lo urgente sin entrar a cada módulo.",
                "El tablero es de lectura: no modifica datos y sigue disponible en modo limitado.",
                List.of("Revise productos por comprar.", "Haga respaldos con frecuencia.", "Use reportes para imprimir listas de compra.")
        );
        side.setPrefWidth(340);
        return new ModuleScaffold("Inicio", "Tablero operativo de la tienda.", content, side, null);
    }

    private Node metric(String title, String value, String description) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-title");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("metric-value");
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("muted-text");
        VBox box = new VBox(6, titleLabel, valueLabel, descriptionLabel);
        box.getStyleClass().add("metric-card");
        box.setPrefWidth(240);
        return box;
    }

    private String money(java.math.BigDecimal value) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(value);
    }
}
