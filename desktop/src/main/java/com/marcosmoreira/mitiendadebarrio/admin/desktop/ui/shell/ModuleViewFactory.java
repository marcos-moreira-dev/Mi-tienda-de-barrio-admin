package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.shell;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppCard;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.EmptyState;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.ayuda.AyudaContextualView;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.caja.CajaDiariaView;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.casosdeuso.CasosDeUsoView;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.catalogos.CatalogosBaseView;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.compras.ComprasEntradasView;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.configuracion.ConfiguracionNegocioView;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.dashboard.DashboardView;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.fiado.FiadoCuentasView;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.licencia.LicenciaView;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.movimientos.MovimientosInventarioView;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.productos.ProductosInventarioView;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.proveedores.ProveedoresView;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.reportes.ReportesOperativosView;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.respaldos.RespaldosView;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.ventas.VentasInternasView;
import javafx.scene.Node;

/**
 * Fábrica de vistas por módulo.
 *
 * La shell principal ya no necesita conocer cada pantalla concreta.
 */
public final class ModuleViewFactory {
    private final AppContext context;

    public ModuleViewFactory(AppContext context) {
        this.context = context;
    }

    public Node create(AppModuleDescriptor module) {
        return switch (module.id()) {
            case "home" -> new DashboardView(context).render();
            case "casosdeuso" -> new CasosDeUsoView().render();
            case "configuracion" -> new ConfiguracionNegocioView(context).render();
            case "catalogos" -> new CatalogosBaseView(context).render();
            case "proveedores" -> new ProveedoresView(context).render();
            case "productos" -> new ProductosInventarioView(context).render();
            case "movimientos" -> new MovimientosInventarioView(context).render();
            case "compras" -> new ComprasEntradasView(context).render();
            case "salidas" -> new VentasInternasView(context).render();
            case "reportes" -> new ReportesOperativosView(context).render();
            case "respaldos" -> new RespaldosView(context).render();
            case "licencia" -> new LicenciaView(context).render();
            case "caja" -> new CajaDiariaView(context).render();
            case "fiado" -> new FiadoCuentasView(context).render();
            case "ayuda" -> new AyudaContextualView(context).render();
            default -> placeholder(module);
        };
    }

    private Node placeholder(AppModuleDescriptor module) {
        return new AppCard(new EmptyState(
                module.label(),
                module.description() + " Este módulo queda para una tanda posterior."
        ));
    }
}
