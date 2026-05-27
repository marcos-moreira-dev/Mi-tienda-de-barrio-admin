package com.marcosmoreira.mitiendadebarrio.admin;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppBootstrap;
import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.loading.LoadingView;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.theme.ThemeStylesheets;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Punto de entrada de Mi tienda de barrio admin.
 *
 * <p>Aplicación autocontenida: JavaFX + core embebido + SQLite.</p>
 */
public final class MiTiendaDeBarrioAdminApp extends Application {

    private AppContext context;

    @Override
    public void start(Stage stage) {
        this.context = AppBootstrap.start();

        LoadingView loadingView = new LoadingView(context);
        Scene scene = new Scene(loadingView.render(stage), 1100, 720);
        ThemeStylesheets.applyTo(scene);

        stage.setTitle("Mi tienda de barrio admin");
        var iconStream = MiTiendaDeBarrioAdminApp.class.getResourceAsStream("/assets/static/icono-operacion-tienda.png");
        if (iconStream != null) {
            stage.getIcons().add(new Image(iconStream));
        }
        stage.setMinWidth(1000);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    @Override
    public void stop() {
        if (context != null) {
            context.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
