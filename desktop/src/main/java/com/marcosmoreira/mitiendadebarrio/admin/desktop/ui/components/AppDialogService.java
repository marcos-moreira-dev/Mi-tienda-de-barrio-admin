package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components;

/** Servicio simple de diálogos para inyectar o sustituir luego sin tocar pantallas. */
public final class AppDialogService {

    public void info(String title, String header, String content) {
        AppDialog.info(title, header, content);
    }

    public void warning(String title, String header, String content) {
        AppDialog.warning(title, header, content);
    }

    public void error(String title, String header, String content) {
        AppDialog.error(title, header, content);
    }
}
