package com.marcosmoreira.mitiendadebarrio.admin.core.application.movimiento;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.movimiento.MovimientoInventario;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.movimiento.TipoMovimientoInventario;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.math.BigDecimal;
import java.util.List;

/** Casos de uso locales para ajustes y trazabilidad de inventario. */
public final class MovimientoInventarioService {
    private final MovimientoInventarioRepository repository;
    private final WriteAccessGuard writeAccessGuard;

    public MovimientoInventarioService(MovimientoInventarioRepository repository) {
        this(repository, null);
    }

    public MovimientoInventarioService(MovimientoInventarioRepository repository, WriteAccessGuard writeAccessGuard) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
    }

    public List<MovimientoInventario> recientes(String query) {
        return repository.findRecent(query, 200);
    }

    public OperationResult<MovimientoInventario> registrarAjuste(Long productoId, TipoMovimientoInventario tipo, BigDecimal cantidad, String motivo, String responsable, String observacion) {
        OperationResult<MovimientoInventario> blocked = bloquearSiNoPuedeEscribir("registrar ajuste de inventario");
        if (blocked != null) return blocked;
        try {
            validar(productoId, tipo, cantidad, motivo);
            MovimientoInventario movimiento = repository.registrarAjuste(productoId, tipo, cantidad, motivo, responsable, observacion);
            return OperationResult.success(movimiento, "Movimiento registrado y stock actualizado.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar el movimiento. Revise el stock disponible y los datos ingresados.");
        }
    }

    private void validar(Long productoId, TipoMovimientoInventario tipo, BigDecimal cantidad, String motivo) {
        if (productoId == null) {
            throw new ValidationException("Debe seleccionar un producto.");
        }
        if (tipo == null) {
            throw new ValidationException("Debe seleccionar el tipo de movimiento.");
        }
        if (tipo == TipoMovimientoInventario.ENTRADA_COMPRA || tipo == TipoMovimientoInventario.SALIDA_VENTA_INTERNA) {
            throw new ValidationException("Este módulo solo registra ajustes manuales, mermas y correcciones. Compras y salidas tienen su propio flujo.");
        }
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("La cantidad debe ser mayor que cero.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ValidationException("Debe escribir un motivo breve para trazabilidad.");
        }
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }
}
