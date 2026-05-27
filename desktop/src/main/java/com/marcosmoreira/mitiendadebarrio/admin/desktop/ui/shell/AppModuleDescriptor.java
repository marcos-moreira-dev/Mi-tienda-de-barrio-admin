package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.shell;

/**
 * Descriptor visual de un módulo de la carcasa.
 *
 * No ejecuta lógica de negocio; solo describe cómo se muestra un acceso de navegación.
 */
public record AppModuleDescriptor(
        String id,
        String label,
        String description,
        boolean enabled
) {
    public static AppModuleDescriptor enabled(String id, String label, String description) {
        return new AppModuleDescriptor(id, label, description, true);
    }

    public static AppModuleDescriptor pending(String id, String label, String description) {
        return new AppModuleDescriptor(id, label, description, false);
    }
}
