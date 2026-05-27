package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.configuracion;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.configuracion.ConfiguracionNegocio;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.ActionBar;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppCard;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppDialog;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.FormGrid;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.InfoPanel;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.ModuleScaffold;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;

/** Módulo vertical inicial: configuración del negocio dueño de la instalación. */
public final class ConfiguracionNegocioView {

    private final AppContext context;

    private TextField nombreField;
    private TextField rucField;
    private TextField responsableField;
    private TextField telefonoField;
    private TextField direccionField;
    private TextField actividadField;
    private TextField monedaField;
    private TextArea observacionArea;

    public ConfiguracionNegocioView(AppContext context) {
        this.context = context;
    }

    public Node render() {
        ConfiguracionNegocio actual = context.configuracionNegocioService().obtenerActual();

        nombreField = textField(actual.nombreComercial(), "Ej. Despensa María");
        rucField = textField(actual.ruc(), "RUC opcional");
        responsableField = textField(actual.responsable(), "Persona responsable");
        telefonoField = textField(actual.telefono(), "Teléfono o WhatsApp");
        direccionField = textField(actual.direccion(), "Dirección del local");
        actividadField = textField(actual.actividad(), "Tienda / despensa de barrio");
        monedaField = textField(actual.moneda(), "USD");
        observacionArea = new TextArea(actual.observacion());
        observacionArea.setPromptText("Observaciones internas: permisos, forma de trabajo, advertencias, etc.");
        observacionArea.setWrapText(true);
        observacionArea.setPrefRowCount(4);

        FormGrid form = new FormGrid();
        form.addField("Nombre comercial *", nombreField);
        form.addField("RUC", rucField);
        form.addField("Responsable", responsableField);
        form.addField("Teléfono", telefonoField);
        form.addField("Dirección", direccionField);
        form.addField("Actividad", actividadField);
        form.addField("Moneda *", monedaField);
        form.addField("Observación", observacionArea);

        AppButton guardarButton = AppButton.primary("Guardar configuración");
        guardarButton.setOnAction(event -> guardar());

        AppButton ayudaButton = AppButton.secondary("¿Qué pongo aquí?");
        ayudaButton.setOnAction(event -> AppDialog.info(
                "Ayuda de configuración",
                "Datos del negocio",
                "Esta pantalla guarda los datos visibles del negocio local. Sirven para encabezados de reportes, soporte, respaldos y configuración inicial. No reemplaza trámites municipales, SRI o ARCSA."
        ));

        AppCard contentCard = new AppCard(form);
        HBox.setHgrow(contentCard, Priority.ALWAYS);

        InfoPanel sidePanel = new InfoPanel(
                "Configuración base",
                "Define la identidad de la tienda antes de cargar productos, compras o reportes.",
                "Regla UX: esta pantalla debe ser tranquila, clara y poco intimidante. Un dueño no técnico debe entenderla sin explicación larga.",
                List.of(
                        "Guardar nombre comercial y moneda antes de operar.",
                        "Usar observaciones para datos documentales o comerciales del local.",
                        "No usar esta pantalla como asesor legal ni tributario."
                )
        );
        sidePanel.setPrefWidth(320);

        return new ModuleScaffold(
                "Configuración del negocio",
                "Datos generales usados por reportes, respaldos, licencia y soporte local.",
                contentCard,
                sidePanel,
                new ActionBar(ayudaButton, guardarButton)
        );
    }

    private TextField textField(String value, String prompt) {
        TextField field = new TextField(value == null ? "" : value);
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private void guardar() {
        ConfiguracionNegocio configuracion = new ConfiguracionNegocio(
                nombreField.getText(),
                rucField.getText(),
                responsableField.getText(),
                telefonoField.getText(),
                direccionField.getText(),
                actividadField.getText(),
                monedaField.getText(),
                observacionArea.getText()
        );
        OperationResult<ConfiguracionNegocio> result = context.configuracionNegocioService().guardar(configuracion);
        if (result.success()) {
            AppDialog.info("Configuración", "Cambios guardados", result.message());
        } else {
            AppDialog.warning("Configuración", "No se pudo guardar", result.message());
        }
    }
}
