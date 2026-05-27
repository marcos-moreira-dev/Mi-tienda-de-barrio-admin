package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.loading;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.StartupReport;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppCard;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.SectionHeader;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.login.LoginView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Pantalla de arranque. */
public final class LoadingView {

    private final AppContext context;

    public LoadingView(AppContext context) {
        this.context = context;
    }

    public Parent render(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("screen-center");

        StartupReport report = context.healthService().checkStartup();
        VBox messages = new VBox(6);
        for (String message : report.messages()) {
            Label label = new Label("• " + message);
            label.getStyleClass().add("muted-text");
            messages.getChildren().add(label);
        }

        ImageView logo = new ImageView();
        var stream = LoadingView.class.getResourceAsStream("/assets/static/logo-mi-tienda-barrio.png");
        if (stream != null) {
            logo.setImage(new Image(stream));
            logo.setFitWidth(260);
            logo.setPreserveRatio(true);
            logo.getStyleClass().add("loading-logo");
        }

        ProgressBar progressBar = new ProgressBar(0.05);
        progressBar.setPrefWidth(360);
        progressBar.getStyleClass().add("loading-progress-bar");

        Label progressLabel = new Label("Preparando entorno local y verificando archivos base...");
        progressLabel.getStyleClass().add("muted-text");

        AppCard card = new AppCard(
                new SectionHeader("Preparando el sistema", "Verificando runtime local, licencia y base de datos SQLite."),
                logo,
                progressBar,
                progressLabel,
                messages
        );
        card.setMaxWidth(700);
        root.setCenter(card);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0.12)),
                new KeyFrame(Duration.millis(350), new KeyValue(progressBar.progressProperty(), 0.48)),
                new KeyFrame(Duration.millis(720), new KeyValue(progressBar.progressProperty(), 0.82)),
                new KeyFrame(Duration.millis(980), new KeyValue(progressBar.progressProperty(), 1.0))
        );
        timeline.play();

        PauseTransition pause = new PauseTransition(Duration.millis(1100));
        pause.setOnFinished(event -> stage.getScene().setRoot(new LoginView(context).render(stage)));
        pause.play();
        return root;
    }
}
