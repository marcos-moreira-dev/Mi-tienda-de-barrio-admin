package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.shell;

import java.util.List;

/**
 * Grupo visual de navegación de la carcasa.
 *
 * Permite agregar o mover módulos sin editar la vista principal del shell.
 */
public record AppModuleGroupDescriptor(
        String title,
        List<String> moduleIds
) {
    public AppModuleGroupDescriptor {
        moduleIds = List.copyOf(moduleIds);
    }

    public static AppModuleGroupDescriptor of(String title, List<String> moduleIds) {
        return new AppModuleGroupDescriptor(title, moduleIds);
    }
}
