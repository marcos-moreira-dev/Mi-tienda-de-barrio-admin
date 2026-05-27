package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.shell;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppCard;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.EmptyState;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.StatusBadge;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.login.LoginView;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.shell.AppModuleDescriptor;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.shell.AppModuleGroupDescriptor;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.shell.AppShellDescriptor;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.shell.ModuleIconResolver;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.shell.ModuleViewFactory;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.shell.WriteModulePolicy;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Shell principal con navegación lateral y marco visual profesional. */
public final class MainShellView {
    private final AppContext context;
    private final AppShellDescriptor shellDescriptor;
    private final ModuleViewFactory moduleViewFactory;
    private final ModuleIconResolver iconResolver = new ModuleIconResolver();
    private final WriteModulePolicy writeModulePolicy = WriteModulePolicy.defaultForMiTiendaDeBarrio();
    private final Map<String, Button> moduleButtons = new HashMap<>();
    private final Map<String, Image> iconCache = new HashMap<>();
    private final List<Label> sidebarGroupLabels = new ArrayList<>();

    private BorderPane root;
    private BorderPane sidebarFrame;
    private VBox sidebarBrandCopy;
    private VBox sidebarFooterInfo;
    private StackPane workspace;
    private Label moduleTitle;
    private Label moduleSubtitle;
    private Label statusMessage;
    private Label statusDetail;
    private Button sidebarCollapseButton;
    private ImageView sidebarBrandIcon;
    private AppButton logoutButton;
    private String activeModuleId = "home";
    private boolean sidebarCollapsed;

    public MainShellView(AppContext context) {
        this(context, AppShellDescriptor.defaultForMiTiendaDeBarrio());
    }

    public MainShellView(AppContext context, AppShellDescriptor shellDescriptor) {
        this.context = context;
        this.shellDescriptor = shellDescriptor;
        this.moduleViewFactory = new ModuleViewFactory(context);
    }

    public Parent render() {
        return render(null);
    }

    public Parent render(Stage stage) {
        this.root = new BorderPane();
        root.getStyleClass().addAll("main-shell", "shell-frame");

        this.workspace = new StackPane();
        workspace.getStyleClass().addAll("workspace", "workspace-host");
        workspace.setMinSize(0, 0);
        workspace.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        root.setLeft(buildSidebar());
        root.setTop(buildTopRegion(stage));
        root.setCenter(workspace);
        root.setBottom(buildStatusbar());

        showHome();
        updateSidebarCollapseState();
        return root;
    }

    private BorderPane buildSidebar() {
        sidebarFrame = new BorderPane();
        sidebarFrame.getStyleClass().add("shell-sidebar-frame");

        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("shell-sidebar");
        sidebar.getChildren().add(buildBrandBlock());

        for (AppModuleGroupDescriptor group : shellDescriptor.navigationGroups()) {
            addGroup(sidebar, group.title(), group.moduleIds());
        }

        ScrollPane scrollPane = new ScrollPane(sidebar);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("shell-sidebar-scroll");
        sidebarFrame.setCenter(scrollPane);
        sidebarFrame.setBottom(buildSidebarFooter());
        return sidebarFrame;
    }

    private Node buildBrandBlock() {
        ImageView icon = loadBrandIcon();
        this.sidebarBrandIcon = icon;
        icon.getStyleClass().add("sidebar-brand-icon");

        Label brand = new Label("Mi tienda de barrio");
        brand.getStyleClass().add("sidebar-brand");
        Label helper = new Label("Admin local · JavaFX + SQLite");
        helper.getStyleClass().add("sidebar-helper");

        sidebarBrandCopy = new VBox(2, brand, helper);
        sidebarBrandCopy.getStyleClass().add("sidebar-brand-copy");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        sidebarCollapseButton = AppButton.ghost("☰");
        sidebarCollapseButton.getStyleClass().add("sidebar-collapse-button");
        sidebarCollapseButton.setOnAction(event -> {
            sidebarCollapsed = !sidebarCollapsed;
            updateSidebarCollapseState();
        });
        sidebarCollapseButton.setTooltip(new Tooltip("Contraer o expandir menú lateral"));

        HBox row = new HBox(12, icon, sidebarBrandCopy, spacer, sidebarCollapseButton);
        row.getStyleClass().add("sidebar-brand-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Node buildSidebarFooter() {
        Label title = new Label("SQLite local · sin nube obligatoria");
        title.getStyleClass().add("sidebar-footer-label");
        title.setWrapText(true);
        Label detail = new Label("Respaldos y reportes quedan disponibles incluso en modo limitado.");
        detail.getStyleClass().add("sidebar-footer-label");
        detail.setWrapText(true);
        sidebarFooterInfo = new VBox(6, title, detail);

        logoutButton = AppButton.ghost("Cerrar sesión");
        logoutButton.getStyleClass().addAll("sidebar-utility-button", "sidebar-logout-button");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setAlignment(Pos.CENTER_LEFT);
        logoutButton.setGraphic(loadIcon("logout"));
        logoutButton.setGraphicTextGap(12);
        logoutButton.setContentDisplay(ContentDisplay.LEFT);
        logoutButton.setOnAction(event -> logout());
        logoutButton.setTooltip(new Tooltip("Cerrar sesión"));

        VBox footer = new VBox(10, sidebarFooterInfo, logoutButton);
        footer.getStyleClass().add("sidebar-footer");
        return footer;
    }

    private void addGroup(VBox sidebar, String title, List<String> moduleIds) {
        Label group = new Label(title);
        group.getStyleClass().add("sidebar-group-label");
        sidebarGroupLabels.add(group);
        sidebar.getChildren().add(group);
        for (String moduleId : moduleIds) {
            findModule(moduleId).ifPresent(module -> sidebar.getChildren().add(buildModuleButton(module)));
        }
    }

    private Button buildModuleButton(AppModuleDescriptor module) {
        Button button = new Button(module.label());
        button.getStyleClass().add("sidebar-module-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinWidth(0);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setGraphic(loadIcon(module.id()));
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setGraphicTextGap(10);
        button.setDisable(!module.enabled());
        button.setTooltip(new Tooltip(module.label()));
        button.setOnAction(event -> showModule(module));
        moduleButtons.put(module.id(), button);
        return button;
    }

    private Node loadIcon(String moduleId) {
        String filename = iconResolver.iconFileFor(moduleId);
        try {
            Image image = iconCache.computeIfAbsent(filename, key -> {
                var stream = MainShellView.class.getResourceAsStream("/assets/images/nav/" + key);
                return stream == null ? null : new Image(stream);
            });
            if (image == null) {
                return fallbackIcon(moduleId);
            }
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(20);
            imageView.setFitHeight(20);
            imageView.setPreserveRatio(true);
            imageView.getStyleClass().add("sidebar-module-icon");
            return imageView;
        } catch (Exception ex) {
            return fallbackIcon(moduleId);
        }
    }

    private Node fallbackIcon(String moduleId) {
        Label label = new Label(moduleId.substring(0, 1).toUpperCase());
        label.getStyleClass().add("sidebar-module-fallback-icon");
        return label;
    }

    private Node buildTopRegion(Stage stage) {
        VBox topRegion = new VBox(buildMenuBar(stage), buildTopbar(stage));
        topRegion.getStyleClass().add("shell-top-region");
        return topRegion;
    }

    private MenuBar buildMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("shell-menubar");

        Menu archivo = new Menu("Archivo");
        MenuItem irInicio = new MenuItem("Inicio");
        irInicio.setOnAction(event -> showHome());
        MenuItem irRespaldos = new MenuItem("Respaldos");
        irRespaldos.setOnAction(event -> findModule("respaldos").ifPresent(this::showModule));
        MenuItem cerrarSesion = new MenuItem("Cerrar sesión");
        cerrarSesion.setOnAction(event -> logout());
        MenuItem salir = new MenuItem("Salir");
        salir.setOnAction(event -> Platform.exit());
        archivo.getItems().addAll(irInicio, irRespaldos, new SeparatorMenuItem(), cerrarSesion, salir);

        Menu ventana = new Menu("Ventana");
        MenuItem pantallaCompleta = new MenuItem("Pantalla completa / salir");
        pantallaCompleta.setOnAction(event -> toggleFullScreen(stage));
        MenuItem maximizar = new MenuItem("Maximizar / restaurar");
        maximizar.setOnAction(event -> {
            Stage target = resolveStage(stage);
            if (target != null) {
                target.setMaximized(!target.isMaximized());
            }
        });
        MenuItem sidebar = new MenuItem("Contraer / expandir menú lateral");
        sidebar.setOnAction(event -> toggleSidebar());
        ventana.getItems().addAll(pantallaCompleta, maximizar, sidebar);

        Menu herramientas = new Menu("Herramientas");
        MenuItem refrescar = new MenuItem("Refrescar módulo actual");
        refrescar.setOnAction(event -> findModule(activeModuleId).ifPresent(this::showModule));
        MenuItem datos = new MenuItem("Abrir respaldos");
        datos.setOnAction(event -> findModule("respaldos").ifPresent(this::showModule));
        herramientas.getItems().addAll(refrescar, datos);

        Menu ayuda = new Menu("Ayuda");
        MenuItem casos = new MenuItem("Casos de uso");
        casos.setOnAction(event -> findModule("casosdeuso").ifPresent(this::showModule));
        MenuItem manual = new MenuItem("Ayuda contextual");
        manual.setOnAction(event -> findModule("ayuda").ifPresent(this::showModule));
        ayuda.getItems().addAll(casos, manual);

        menuBar.getMenus().addAll(archivo, ventana, herramientas, ayuda);
        return menuBar;
    }

    private Node buildTopbar(Stage stage) {
        moduleTitle = new Label(shellDescriptor.productName());
        moduleTitle.getStyleClass().add("shell-title");
        moduleSubtitle = new Label(shellDescriptor.productSubtitle());
        moduleSubtitle.getStyleClass().add("shell-subtitle");
        moduleSubtitle.setWrapText(true);
        VBox titleBox = new VBox(2, moduleTitle, moduleSubtitle);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StatusBadge localBadge = new StatusBadge("Modo local");
        StatusBadge dbBadge = new StatusBadge("SQLite");
        StatusBadge licenseBadge = new StatusBadge(context.licenseService().currentStatus().label());

        HBox actions = new HBox(10, localBadge, dbBadge, licenseBadge);
        actions.getStyleClass().add("shell-topbar-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox topbar = new HBox(18, titleBox, spacer, actions);
        topbar.getStyleClass().add("shell-topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);
        return topbar;
    }

    private Node buildStatusbar() {
        statusMessage = new Label("Listo");
        statusMessage.getStyleClass().add("statusbar-message");
        statusDetail = new Label("Aplicación autocontenida para tiendas, despensas, víveres y pequeños minimarkets.");
        statusDetail.getStyleClass().add("statusbar-detail");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label runtime = new Label("Cliente real limpio · presentación separada");
        runtime.getStyleClass().add("statusbar-detail");
        HBox bar = new HBox(12, statusMessage, statusDetail, spacer, runtime);
        bar.getStyleClass().add("shell-statusbar");
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private Optional<AppModuleDescriptor> findModule(String id) {
        return shellDescriptor.findModule(id);
    }

    private void showModule(AppModuleDescriptor module) {
        updateChrome(module);
        if (writeModulePolicy.requiresWriteAccess(module.id()) && !context.writeAccessGuard().canWrite()) {
            show(new AppCard(new EmptyState(
                    "Licencia en modo limitado",
                    "Puede consultar inicio, reportes, respaldos, licencia y ayuda. Para registrar o modificar información debe renovar la licencia."
            )));
            return;
        }
        show(moduleViewFactory.create(module));
    }

    private void updateChrome(AppModuleDescriptor module) {
        activeModuleId = module.id();
        moduleTitle.setText(module.label());
        moduleSubtitle.setText(module.description());
        statusMessage.setText(module.label());
        statusDetail.setText(module.description());
        moduleButtons.forEach((id, button) -> button.getStyleClass().remove("sidebar-module-button-active"));
        Button activeButton = moduleButtons.get(activeModuleId);
        if (activeButton != null && !activeButton.getStyleClass().contains("sidebar-module-button-active")) {
            activeButton.getStyleClass().add("sidebar-module-button-active");
        }
    }

    private void showHome() {
        findModule("home").ifPresent(this::showModule);
    }

    private void show(Node content) {
        StackPane shell = new StackPane(content);
        shell.getStyleClass().add("workspace-card-shell");
        shell.setMinSize(0, 0);
        shell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ScrollPane scrollPane = new ScrollPane(shell);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("workspace-scroll");
        workspace.getChildren().setAll(scrollPane);
    }

    private void logout() {
        if (root == null || root.getScene() == null) {
            return;
        }
        Stage stage = (Stage) root.getScene().getWindow();
        stage.getScene().setRoot(new LoginView(context).render(stage));
    }

    private void toggleFullScreen(Stage preferredStage) {
        Stage stage = preferredStage;
        if (stage == null && root != null && root.getScene() != null) {
            stage = (Stage) root.getScene().getWindow();
        }
        if (stage == null) {
            return;
        }
        stage.setFullScreen(!stage.isFullScreen());
        updateFullScreenButton(stage);
    }

    private Stage resolveStage(Stage preferredStage) {
        if (preferredStage != null) {
            return preferredStage;
        }
        if (root != null && root.getScene() != null) {
            return (Stage) root.getScene().getWindow();
        }
        return null;
    }

    private void updateFullScreenButton(Stage stage) {
        // La opción vive ahora en el MenuBar para mantener el topbar limpio.
    }

    private void toggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        updateSidebarCollapseState();
    }

    private void updateSidebarCollapseState() {
        if (sidebarFrame == null) {
            return;
        }
        if (sidebarCollapsed) {
            sidebarFrame.getStyleClass().add("shell-sidebar-frame-collapsed");
            sidebarFrame.setPrefWidth(104);
            sidebarFrame.setMinWidth(104);
            sidebarFrame.setMaxWidth(104);
            sidebarBrandCopy.setManaged(false);
            sidebarBrandCopy.setVisible(false);
            if (sidebarBrandIcon != null) {
                sidebarBrandIcon.setFitWidth(38);
                sidebarBrandIcon.setFitHeight(38);
            }
            sidebarFooterInfo.setManaged(false);
            sidebarFooterInfo.setVisible(false);
            if (logoutButton != null) {
                logoutButton.setText("");
                logoutButton.setPrefWidth(64);
                logoutButton.setAlignment(Pos.CENTER);
                logoutButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                logoutButton.setGraphicTextGap(0);
                resizeButtonGraphic(logoutButton, 22);
                logoutButton.getStyleClass().add("sidebar-footer-button-collapsed");
            }
            for (Label label : sidebarGroupLabels) {
                label.setManaged(false);
                label.setVisible(false);
            }
            moduleButtons.values().forEach(button -> {
                button.setText("");
                button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                button.setGraphicTextGap(0);
                button.setAlignment(Pos.CENTER);
                resizeButtonGraphic(button, 22);
                button.getStyleClass().add("sidebar-module-button-collapsed");
            });
            sidebarCollapseButton.setText("☰");
            sidebarCollapseButton.setTooltip(new Tooltip("Expandir menú lateral"));
        } else {
            sidebarFrame.getStyleClass().remove("shell-sidebar-frame-collapsed");
            sidebarFrame.setPrefWidth(320);
            sidebarFrame.setMinWidth(320);
            sidebarFrame.setMaxWidth(320);
            sidebarBrandCopy.setManaged(true);
            sidebarBrandCopy.setVisible(true);
            if (sidebarBrandIcon != null) {
                sidebarBrandIcon.setFitWidth(48);
                sidebarBrandIcon.setFitHeight(48);
            }
            sidebarFooterInfo.setManaged(true);
            sidebarFooterInfo.setVisible(true);
            if (logoutButton != null) {
                logoutButton.setText("Cerrar sesión");
                logoutButton.setPrefWidth(Region.USE_COMPUTED_SIZE);
                logoutButton.setAlignment(Pos.CENTER_LEFT);
                logoutButton.setContentDisplay(ContentDisplay.LEFT);
                logoutButton.setGraphicTextGap(12);
                resizeButtonGraphic(logoutButton, 20);
                logoutButton.getStyleClass().remove("sidebar-footer-button-collapsed");
            }
            for (Label label : sidebarGroupLabels) {
                label.setManaged(true);
                label.setVisible(true);
            }
            moduleButtons.forEach((id, button) -> {
                findModule(id).ifPresent(module -> button.setText(module.label()));
                button.setContentDisplay(ContentDisplay.LEFT);
                button.setGraphicTextGap(10);
                button.setAlignment(Pos.CENTER_LEFT);
                resizeButtonGraphic(button, 20);
                button.getStyleClass().remove("sidebar-module-button-collapsed");
            });
            sidebarCollapseButton.setText("☰");
            sidebarCollapseButton.setTooltip(new Tooltip("Contraer menú lateral"));
        }
    }


    private void resizeButtonGraphic(Button button, double size) {
        if (button == null || button.getGraphic() == null) {
            return;
        }
        if (button.getGraphic() instanceof ImageView imageView) {
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
        }
    }

    private ImageView loadBrandIcon() {
        var stream = MainShellView.class.getResourceAsStream("/assets/static/icono-operacion-tienda.png");
        ImageView imageView = new ImageView();
        if (stream != null) {
            imageView.setImage(new Image(stream));
        }
        imageView.setFitWidth(48);
        imageView.setFitHeight(48);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
    }
}
