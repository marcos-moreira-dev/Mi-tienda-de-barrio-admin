package com.marcosmoreira.mitiendadebarrio.admin.core.application.producto;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.EstadoProducto;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.Producto;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.math.BigDecimal;
import java.util.List;

/** Casos de uso locales para productos e inventario base. */
public final class ProductoService {
    private final ProductoRepository repository;
    private final WriteAccessGuard writeAccessGuard;

    public ProductoService(ProductoRepository repository) {
        this(repository, null);
    }

    public ProductoService(ProductoRepository repository, WriteAccessGuard writeAccessGuard) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
    }

    public List<Producto> listar(String busqueda, boolean incluirInactivos) {
        return repository.findAll(busqueda, incluirInactivos);
    }

    public OperationResult<Producto> guardar(Producto producto) {
        OperationResult<Producto> blocked = bloquearSiNoPuedeEscribir("guardar producto");
        if (blocked != null) return blocked;
        try {
            validar(producto);
            Producto guardado = repository.save(producto);
            return OperationResult.success(guardado, "Producto guardado correctamente.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo guardar el producto. Revise código interno, campos obligatorios y catálogos relacionados.");
        }
    }

    public OperationResult<Void> desactivar(long id) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("desactivar producto");
        if (blocked != null) return blocked;
        repository.updateEstado(id, EstadoProducto.INACTIVO);
        return OperationResult.success(null, "Producto desactivado.");
    }

    public OperationResult<Void> reactivar(long id) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("reactivar producto");
        if (blocked != null) return blocked;
        repository.updateEstado(id, EstadoProducto.ACTIVO);
        return OperationResult.success(null, "Producto reactivado.");
    }

    private void validar(Producto producto) {
        if (producto.nombre() == null || producto.nombre().isBlank()) {
            throw new ValidationException("El nombre del producto es obligatorio.");
        }
        if (producto.categoriaId() == null) {
            throw new ValidationException("Debe seleccionar una categoría.");
        }
        if (producto.unidadMedidaId() == null) {
            throw new ValidationException("Debe seleccionar una unidad de medida.");
        }
        validarNoNegativo(producto.precioCompraReferencia(), "El precio de compra no puede ser negativo.");
        validarNoNegativo(producto.precioVenta(), "El precio de venta no puede ser negativo.");
        validarNoNegativo(producto.stockActual(), "El stock actual no puede ser negativo.");
        validarNoNegativo(producto.stockMinimo(), "El stock mínimo no puede ser negativo.");
        if (producto.stockObjetivo() != null && producto.stockMinimo() != null && producto.stockObjetivo().compareTo(producto.stockMinimo()) < 0) {
            throw new ValidationException("El stock objetivo no puede ser menor que el stock mínimo.");
        }
    }

    private void validarNoNegativo(BigDecimal value, String message) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(message);
        }
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }
}
