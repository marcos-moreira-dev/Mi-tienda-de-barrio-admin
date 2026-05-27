package com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.screens.productos;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.AppContext;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.Producto;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppCard;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.AppDialog;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.InfoPanel;
import com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.components.ModuleScaffold;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/** Vista de productos e inventario base. */
public final class ProductosInventarioView {
    private final AppContext context;
    private final ProductoFilterPane filterPane;
    private final ProductoListPane listPane;
    private final ProductoFormPane formPane;

    private Producto seleccionado;

    public ProductosInventarioView(AppContext context) {
        this.context = context;
        this.filterPane = new ProductoFilterPane(this::cargarDatos);
        this.listPane = new ProductoListPane(this::seleccionar);
        this.formPane = new ProductoFormPane(this::limpiarFormulario, this::guardar, this::cambiarEstado);
    }

    public Node render() {
        cargarCombos();
        cargarDatos();

        VBox left = new VBox(8, filterPane, listPane);
        left.setPadding(new Insets(8));
        left.setPrefWidth(380);
        VBox.setVgrow(listPane, Priority.ALWAYS);

        InfoPanel sidePanel = new InfoPanel(
                "Inventario base",
                "Los productos conectan catálogos, proveedores, stock y reportes.",
                "Esta tanda separa filtros, lista y formulario para mantener la pantalla editable sin convertirla en una clase monolítica.",
                List.of(
                        "Stock mínimo dispara alertas.",
                        "Stock objetivo sirve para calcular cuánto comprar.",
                        "Lote/vencimiento se marca solo cuando el producto lo necesita."
                )
        );
        sidePanel.setPrefWidth(330);

        HBox content = new HBox(12, left, formPane);
        HBox.setHgrow(formPane, Priority.ALWAYS);

        return new ModuleScaffold(
                "Productos / Inventario",
                "Catálogo maestro de productos, precios de referencia, stock base y metadatos operativos.",
                new AppCard(content),
                sidePanel,
                null
        );
    }

    private void cargarCombos() {
        formPane.setCatalogos(
                context.categoriaService().listar(false),
                context.marcaService().listar(false),
                context.unidadMedidaService().listar(false),
                context.proveedorService().listar(false)
        );
    }

    private void cargarDatos() {
        listPane.setProductos(context.productoService().listar(filterPane.query(), filterPane.includeInactive()));
    }

    private void seleccionar(Producto item) {
        seleccionado = item;
        if (item != null) {
            formPane.writeProducto(item);
        }
    }

    private void limpiarFormulario() {
        seleccionado = null;
        listPane.clearSelection();
        formPane.clear();
    }

    private void guardar() {
        Producto producto = formPane.readData().toProducto(seleccionado);
        OperationResult<Producto> result = context.productoService().guardar(producto);
        if (result.success()) {
            AppDialog.info("Productos", "Cambios guardados", result.message());
            cargarDatos();
            result.data().ifPresent(this::seleccionar);
        } else {
            AppDialog.warning("Productos", "No se pudo guardar", result.message());
        }
    }

    private void cambiarEstado() {
        if (seleccionado == null || seleccionado.id() == null) {
            AppDialog.warning("Productos", "Seleccione un producto", "Debe seleccionar un producto existente antes de cambiar su estado.");
            return;
        }
        OperationResult<Void> result = seleccionado.estado().dbValue().equals("ACTIVO")
                ? context.productoService().desactivar(seleccionado.id())
                : context.productoService().reactivar(seleccionado.id());
        AppDialog.info("Productos", "Estado actualizado", result.message());
        cargarDatos();
        limpiarFormulario();
    }
}
