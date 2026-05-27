package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.shell;

import java.util.List;
import java.util.Optional;

/**
 * Descriptor parametrizable de la carcasa visual de la aplicación.
 * Permite reutilizar la misma shell en otra app local cambiando nombres, subtítulo, módulos y grupos de navegación.
 */
public record AppShellDescriptor(
        String productName,
        String productSubtitle,
        String homeTitle,
        String homeMessage,
        List<AppModuleDescriptor> modules,
        List<AppModuleGroupDescriptor> navigationGroups
) {
    public AppShellDescriptor {
        modules = List.copyOf(modules);
        navigationGroups = List.copyOf(navigationGroups);
    }

    public Optional<AppModuleDescriptor> findModule(String id) {
        return modules.stream().filter(module -> module.id().equals(id)).findFirst();
    }

    public static AppShellDescriptor defaultForMiTiendaDeBarrio() {
        List<AppModuleDescriptor> modules = List.of(
                AppModuleDescriptor.enabled("home", "Inicio", "Resumen operativo local."),
                AppModuleDescriptor.enabled("casosdeuso", "Casos de uso", "Guía rápida de recorridos puntuales para entender la operación."),
                AppModuleDescriptor.enabled("configuracion", "Configuración", "Identidad del negocio y datos para reportes."),
                AppModuleDescriptor.enabled("catalogos", "Catálogos", "Categorías, marcas y unidades de medida."),
                AppModuleDescriptor.enabled("proveedores", "Proveedores", "Contactos y fuentes de abastecimiento."),
                AppModuleDescriptor.enabled("productos", "Productos", "Catálogo, búsqueda, stock mínimo, foto y alertas."),
                AppModuleDescriptor.enabled("movimientos", "Movimientos", "Ajustes, mermas y trazabilidad de inventario."),
                AppModuleDescriptor.enabled("compras", "Compras", "Entradas de mercadería, proveedor, costo, lote y vencimiento."),
                AppModuleDescriptor.enabled("salidas", "Salidas", "Descargo operativo de stock sin reemplazar facturación."),
                AppModuleDescriptor.enabled("reportes", "Reportes", "Productos por comprar, bajo stock, vencimientos, compras y ventas internas."),
                AppModuleDescriptor.enabled("respaldos", "Respaldos", "Backup, restauración, carpeta local y advertencias por apagones."),
                AppModuleDescriptor.enabled("licencia", "Licencia", "Activación local y modo limitado ético."),
                AppModuleDescriptor.enabled("caja", "Caja", "Caja diaria opcional: ingresos, egresos y cierre."),
                AppModuleDescriptor.enabled("fiado", "Fiado", "Clientes, cuentas por cobrar y abonos simples."),
                AppModuleDescriptor.enabled("ayuda", "Ayuda", "Mini manual integrado y explicación por módulo.")
        );

        List<AppModuleGroupDescriptor> groups = List.of(
                AppModuleGroupDescriptor.of("OPERACIÓN", List.of("home", "casosdeuso", "productos", "compras", "salidas", "movimientos")),
                AppModuleGroupDescriptor.of("ADMINISTRACIÓN", List.of("configuracion", "catalogos", "proveedores", "caja", "fiado")),
                AppModuleGroupDescriptor.of("CONTROL", List.of("reportes", "respaldos", "licencia", "ayuda"))
        );

        return new AppShellDescriptor(
                "Mi tienda de barrio admin",
                "Aplicación autocontenida · JavaFX + Core embebido + SQLite",
                "Sistema listo",
                "La carcasa es parametrizable y ya cuenta con configuración, catálogos, proveedores, productos, movimientos, compras, salidas internas, reportes, respaldos y licencia local.",
                modules,
                groups
        );
    }
}
