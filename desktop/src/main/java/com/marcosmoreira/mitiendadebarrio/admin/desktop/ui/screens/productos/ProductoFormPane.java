package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.productos;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.Categoria;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.Marca;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo.UnidadMedida;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.Producto;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.proveedor.Proveedor;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppButton;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppFormFactory;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Formulario de producto separado de la vista contenedora. */
public final class ProductoFormPane extends VBox {
    private final TextField codigoField = AppFormFactory.textField("Código interno opcional");
    private final TextField nombreField = AppFormFactory.textField("Nombre comercial del producto");
    private final TextField presentacionField = AppFormFactory.textField("Ej. 1 litro, caja x12, funda, unidad");
    private final ComboBox<Categoria> categoriaCombo = AppFormFactory.comboBox(List.of());
    private final ComboBox<Marca> marcaCombo = AppFormFactory.comboBox(List.of());
    private final ComboBox<UnidadMedida> unidadCombo = AppFormFactory.comboBox(List.of());
    private final ComboBox<Proveedor> proveedorCombo = AppFormFactory.comboBox(List.of());
    private final TextField precioCompraField = AppFormFactory.textField("0.00");
    private final TextField precioVentaField = AppFormFactory.textField("0.00");
    private final TextField stockActualField = AppFormFactory.textField("0");
    private final TextField stockMinimoField = AppFormFactory.textField("0");
    private final TextField stockObjetivoField = AppFormFactory.textField("Opcional");
    private final TextField rutaFotoField = AppFormFactory.textField("Ruta de imagen opcional");
    private final CheckBox manejaLoteCheck = AppFormFactory.checkBox("Maneja lote");
    private final CheckBox manejaVencimientoCheck = AppFormFactory.checkBox("Maneja vencimiento");
    private final CheckBox perecibleCheck = AppFormFactory.checkBox("Perecible");
    private final CheckBox refrigeradoCheck = AppFormFactory.checkBox("Refrigerado");
    private final TextArea descripcionArea = AppFormFactory.textArea("Descripción corta para diferenciar productos parecidos.", 2);
    private final TextArea observacionArea = AppFormFactory.textArea("Observaciones internas: proveedor habitual, sustitutos, alertas, etc.", 2);

    public ProductoFormPane(Runnable onNuevo, Runnable onGuardar, Runnable onCambiarEstado) {
        configurarCombos();
        AppButton nuevoButton = AppButton.ghost("Nuevo");
        nuevoButton.setOnAction(event -> onNuevo.run());
        AppButton guardarButton = AppButton.primary("Guardar producto");
        guardarButton.setOnAction(event -> onGuardar.run());
        AppButton cambiarEstadoButton = AppButton.secondary("Activar / desactivar");
        cambiarEstadoButton.setOnAction(event -> onCambiarEstado.run());

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(8);
        int row = 0;
        add(formGrid, row++, "Código", codigoField, "Nombre *", nombreField);
        add(formGrid, row++, "Categoría *", categoriaCombo, "Marca", marcaCombo);
        add(formGrid, row++, "Unidad *", unidadCombo, "Proveedor principal", proveedorCombo);
        add(formGrid, row++, "Presentación", presentacionField, "Foto", rutaFotoField);
        add(formGrid, row++, "Precio compra", precioCompraField, "Precio venta", precioVentaField);
        add(formGrid, row++, "Stock actual", stockActualField, "Stock mínimo", stockMinimoField);
        add(formGrid, row++, "Stock objetivo", stockObjetivoField, "", new HBox(8, manejaLoteCheck, manejaVencimientoCheck));
        add(formGrid, row++, "Condición", new HBox(8, perecibleCheck, refrigeradoCheck), "", new Label(""));

        setSpacing(8);
        setPadding(new Insets(8));
        getChildren().addAll(
                formGrid,
                new Label("Descripción"), descripcionArea,
                new Label("Observación"), observacionArea,
                new HBox(8, nuevoButton, guardarButton, cambiarEstadoButton)
        );
        HBox.setHgrow(this, Priority.ALWAYS);
    }

    private void add(GridPane grid, int row, String labelA, Node nodeA, String labelB, Node nodeB) {
        grid.add(new Label(labelA), 0, row);
        grid.add(nodeA, 1, row);
        grid.add(new Label(labelB), 2, row);
        grid.add(nodeB, 3, row);
    }

    public void setCatalogos(List<Categoria> categorias, List<Marca> marcas, List<UnidadMedida> unidades, List<Proveedor> proveedores) {
        categoriaCombo.getItems().setAll(categorias);
        marcaCombo.getItems().setAll(marcas);
        unidadCombo.getItems().setAll(unidades);
        proveedorCombo.getItems().setAll(proveedores);
    }

    public ProductoFormData readData() {
        return new ProductoFormData(
                codigoField.getText(),
                nombreField.getText(),
                descripcionArea.getText(),
                categoriaCombo.getValue(),
                marcaCombo.getValue(),
                unidadCombo.getValue(),
                proveedorCombo.getValue(),
                presentacionField.getText(),
                decimalOrZero(precioCompraField.getText()),
                decimalOrZero(precioVentaField.getText()),
                decimalOrZero(stockActualField.getText()),
                decimalOrZero(stockMinimoField.getText()),
                decimalOrNull(stockObjetivoField.getText()),
                manejaLoteCheck.isSelected(),
                manejaVencimientoCheck.isSelected(),
                perecibleCheck.isSelected(),
                refrigeradoCheck.isSelected(),
                rutaFotoField.getText(),
                observacionArea.getText()
        );
    }

    public void writeProducto(Producto item) {
        if (item == null) {
            return;
        }
        codigoField.setText(item.codigoInterno());
        nombreField.setText(item.nombre());
        descripcionArea.setText(item.descripcion());
        presentacionField.setText(item.presentacion());
        precioCompraField.setText(item.precioCompraReferencia().toPlainString());
        precioVentaField.setText(item.precioVenta().toPlainString());
        stockActualField.setText(item.stockActual().toPlainString());
        stockMinimoField.setText(item.stockMinimo().toPlainString());
        stockObjetivoField.setText(item.stockObjetivo() == null ? "" : item.stockObjetivo().toPlainString());
        rutaFotoField.setText(item.rutaFoto());
        manejaLoteCheck.setSelected(item.manejaLote());
        manejaVencimientoCheck.setSelected(item.manejaVencimiento());
        perecibleCheck.setSelected(item.perecible());
        refrigeradoCheck.setSelected(item.refrigerado());
        observacionArea.setText(item.observacion());
        selectById(categoriaCombo, item.categoriaId(), Categoria::id);
        selectById(marcaCombo, item.marcaId(), Marca::id);
        selectById(unidadCombo, item.unidadMedidaId(), UnidadMedida::id);
        selectById(proveedorCombo, item.proveedorPrincipalId(), Proveedor::id);
    }

    public void clear() {
        codigoField.clear();
        nombreField.clear();
        descripcionArea.clear();
        presentacionField.clear();
        precioCompraField.setText("0");
        precioVentaField.setText("0");
        stockActualField.setText("0");
        stockMinimoField.setText("0");
        stockObjetivoField.clear();
        rutaFotoField.clear();
        manejaLoteCheck.setSelected(false);
        manejaVencimientoCheck.setSelected(false);
        perecibleCheck.setSelected(false);
        refrigeradoCheck.setSelected(false);
        observacionArea.clear();
        categoriaCombo.getSelectionModel().clearSelection();
        marcaCombo.getSelectionModel().clearSelection();
        unidadCombo.getSelectionModel().clearSelection();
        proveedorCombo.getSelectionModel().clearSelection();
    }

    private void configurarCombos() {
        configurarCombo(categoriaCombo, Categoria::nombre);
        configurarCombo(marcaCombo, marca -> marca == null ? "Sin marca" : marca.nombre());
        configurarCombo(unidadCombo, UnidadMedida::nombre);
        configurarCombo(proveedorCombo, proveedor -> proveedor == null ? "Sin proveedor" : proveedor.nombre());
    }

    private <T> void configurarCombo(ComboBox<T> combo, Function<T, String> labelProvider) {
        combo.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : labelProvider.apply(item));
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : labelProvider.apply(item));
            }
        });
    }

    private <T> void selectById(ComboBox<T> combo, Long id, Function<T, Long> idGetter) {
        if (id == null) {
            combo.getSelectionModel().clearSelection();
            return;
        }
        combo.getItems().stream()
                .filter(item -> Objects.equals(idGetter.apply(item), id))
                .findFirst()
                .ifPresent(combo::setValue);
    }

    private BigDecimal decimalOrZero(String value) {
        BigDecimal parsed = decimalOrNull(value);
        return parsed == null ? BigDecimal.ZERO : parsed;
    }

    private BigDecimal decimalOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.strip().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }
}
