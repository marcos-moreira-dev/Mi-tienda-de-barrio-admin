package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.login;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.seguridad.SesionUsuarioLocal;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.shell.MainShellView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Login local de la aplicación. */
public final class LoginView {

    private static final String INITIAL_USER = "admin";

    private final AppContext context;

    public LoginView(AppContext context) {
        this.context = context;
    }

    public Parent render(Stage stage) {
        HBox root = new HBox(0);
        root.getStyleClass().addAll("login-root", "login-content");
        root.setFillHeight(true);

        StackPane brandWrapper = new StackPane();
        brandWrapper.getStyleClass().add("login-brand-wrapper");
        brandWrapper.setMinWidth(430);
        brandWrapper.setPrefWidth(560);
        HBox.setHgrow(brandWrapper, Priority.ALWAYS);

        Region photoBackground = new Region();
        photoBackground.getStyleClass().add("login-brand-photo-background");
        photoBackground.setMouseTransparent(true);
        brandWrapper.getChildren().add(photoBackground);

        Region tint = new Region();
        tint.getStyleClass().add("login-brand-tint");
        tint.setMouseTransparent(true);
        brandWrapper.getChildren().add(tint);

        VBox brandPanel = buildBrandCopy();
        StackPane.setAlignment(brandPanel, Pos.CENTER_LEFT);
        StackPane.setMargin(brandPanel, new Insets(32, 56, 32, 64));
        brandWrapper.getChildren().add(brandPanel);

        StackPane formShell = new StackPane(buildFormPanel(stage));
        formShell.getStyleClass().add("login-form-shell");
        formShell.setMinWidth(500);
        formShell.setPrefWidth(580);
        HBox.setHgrow(formShell, Priority.ALWAYS);

        root.getChildren().addAll(brandWrapper, formShell);
        return root;
    }

    private VBox buildBrandCopy() {
        Node logo = buildImage("/assets/static/logo-mi-tienda-barrio.png", 310, "login-brand-logo");

        Label brandName = new Label("Mi tienda de barrio admin");
        brandName.getStyleClass().add("login-brand-name");

        Label title = new Label("Control local para operar como minimarket.");
        title.getStyleClass().add("login-brand-title");
        title.setWrapText(true);
        title.setMaxWidth(520);

        Label helper = new Label(
                "Inventario, compras, salidas internas, caja, fiado, reportes, respaldos y licencia local en una sola aplicación autocontenida."
        );
        helper.getStyleClass().add("login-brand-helper");
        helper.setWrapText(true);
        helper.setMaxWidth(520);

        Label footer = new Label("Aplicación para cliente real · modo presentación separado por script");
        footer.getStyleClass().add("login-brand-footer");
        footer.setWrapText(true);

        VBox panel = new VBox(18, logo, brandName, title, helper, footer);
        panel.getStyleClass().add("login-brand-panel");
        return panel;
    }

    private VBox buildFormPanel(Stage stage) {
        Label title = new Label("Ingreso local");
        title.getStyleClass().add("login-form-title");

        Label subtitle = new Label("Acceso inicial para operar la aplicación en esta computadora.");
        subtitle.getStyleClass().add("login-form-subtitle");
        subtitle.setWrapText(true);

        Label runtimeBadge = new Label("Runtime local · SQLite");
        runtimeBadge.getStyleClass().add("runtime-badge");

        TextField userField = new TextField(INITIAL_USER);
        userField.setPromptText("Usuario");
        userField.setMaxWidth(360);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Contraseña");
        passwordField.setMaxWidth(360);

        Label credentialHint = new Label("Credenciales iniciales: usuario admin / contraseña admin123456");
        credentialHint.getStyleClass().add("login-credential-hint");
        credentialHint.setWrapText(true);
        credentialHint.setMaxWidth(390);

        Label feedback = new Label("Ingrese las credenciales locales para continuar.");
        feedback.getStyleClass().add("login-feedback");
        feedback.setWrapText(true);
        feedback.setMaxWidth(390);

        AppButton loginButton = AppButton.primary("Entrar al sistema");
        loginButton.setPrefWidth(190);
        loginButton.setOnAction(event -> validarIngreso(stage, userField, passwordField, feedback));
        passwordField.setOnAction(event -> validarIngreso(stage, userField, passwordField, feedback));

        AppButton clearButton = AppButton.secondary("Limpiar");
        clearButton.setPrefWidth(120);
        clearButton.setOnAction(event -> {
            passwordField.clear();
            feedback.getStyleClass().removeAll("login-feedback-error", "login-feedback-success");
            if (!feedback.getStyleClass().contains("login-feedback")) {
                feedback.getStyleClass().add("login-feedback");
            }
            feedback.setText("Ingrese las credenciales locales para continuar.");
            passwordField.requestFocus();
        });

        HBox actions = new HBox(12, clearButton, loginButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        Label support = new Label(
                "Este acceso usa usuarios, roles y permisos locales guardados en la base SQLite de esta computadora."
        );
        support.getStyleClass().add("login-support");
        support.setWrapText(true);
        support.setMaxWidth(390);

        VBox form = new VBox(16, title, subtitle, runtimeBadge, userField, passwordField, actions, feedback, credentialHint, support);
        form.getStyleClass().add("login-form-panel");
        form.setMaxWidth(420);
        form.setPrefWidth(420);
        form.setAlignment(Pos.CENTER_LEFT);
        return form;
    }

    private void validarIngreso(Stage stage, TextField userField, PasswordField passwordField, Label feedback) {
        String user = userField.getText() == null ? "" : userField.getText().strip();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        feedback.getStyleClass().removeAll("login-feedback-error", "login-feedback-success");

        var result = context.usuarioLocalService().autenticar(user, password);
        if (result.success()) {
            SesionUsuarioLocal sesion = result.data().orElseThrow();
            feedback.getStyleClass().add("login-feedback-success");
            feedback.setText("Acceso correcto. Bienvenido, " + sesion.nombreParaMostrar() + ".");
            stage.getScene().setRoot(new MainShellView(context).render(stage));
            return;
        }

        feedback.getStyleClass().add("login-feedback-error");
        feedback.setText(result.message());
        passwordField.clear();
        passwordField.requestFocus();
    }

    private Node buildImage(String resourcePath, double fitWidth, String styleClass) {
        ImageView imageView = buildImageView(resourcePath, fitWidth);
        imageView.getStyleClass().add(styleClass);
        return imageView;
    }

    private ImageView buildImageView(String resourcePath, double fitWidth) {
        var stream = LoginView.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            return new ImageView();
        }
        ImageView imageView = new ImageView(new Image(stream));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(fitWidth);
        imageView.setSmooth(true);
        imageView.setCache(true);
        return imageView;
    }
}
