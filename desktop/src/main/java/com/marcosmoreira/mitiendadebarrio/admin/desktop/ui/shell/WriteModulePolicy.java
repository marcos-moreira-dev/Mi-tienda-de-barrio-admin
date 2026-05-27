package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.shell;

import java.util.Set;

/**
 * Política visual de módulos que realizan escritura.
 *
 * La shell la usa para aplicar el modo limitado sin quemar reglas en la vista.
 */
public final class WriteModulePolicy {
    private final Set<String> writeModuleIds;

    private WriteModulePolicy(Set<String> writeModuleIds) {
        this.writeModuleIds = Set.copyOf(writeModuleIds);
    }

    public static WriteModulePolicy defaultForMiTiendaDeBarrio() {
        return new WriteModulePolicy(Set.of(
                "configuracion",
                "catalogos",
                "proveedores",
                "productos",
                "movimientos",
                "compras",
                "salidas",
                "caja",
                "fiado"
        ));
    }

    public boolean requiresWriteAccess(String moduleId) {
        return writeModuleIds.contains(moduleId);
    }
}
