package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.shell;

/**
 * Resuelve iconos de navegación sin acoplar la shell a una lista de switch/case.
 */
public final class ModuleIconResolver {
    public String iconFileFor(String moduleId) {
        return switch (moduleId) {
            case "home" -> "home.png";
            case "casosdeuso" -> "file-text.png";
            case "configuracion" -> "settings.png";
            case "catalogos" -> "list.png";
            case "proveedores" -> "building.png";
            case "productos" -> "folder.png";
            case "movimientos" -> "kanban.png";
            case "compras" -> "briefcase.png";
            case "salidas" -> "target.png";
            case "reportes" -> "file-text.png";
            case "respaldos" -> "wallet.png";
            case "licencia" -> "ticket.png";
            case "caja" -> "wallet.png";
            case "fiado" -> "handshake.png";
            case "ayuda" -> "help.png";
            case "logout" -> "logout.png";
            default -> "folder.png";
        };
    }
}
