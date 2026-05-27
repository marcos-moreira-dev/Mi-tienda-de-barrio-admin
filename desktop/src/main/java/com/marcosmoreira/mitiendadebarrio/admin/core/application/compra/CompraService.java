package com.marcosmoreira.mitiendadebarrio.admin.core.application.compra;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.auditoria.AuditoriaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.Compra;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.CuentaPorPagar;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.DetalleCompraAvanzada;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.RegistroCompraAvanzada;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.RegistroCompraSimple;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra.RegistroPagoProveedor;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.math.BigDecimal;
import java.util.List;

/** Casos de uso locales para compras, entradas de mercadería y cuentas por pagar. */
public final class CompraService {
    private final CompraRepository repository;
    private final WriteAccessGuard writeAccessGuard;
    private final AuditoriaService auditoriaService;

    public CompraService(CompraRepository repository) {
        this(repository, null, null);
    }

    public CompraService(CompraRepository repository, WriteAccessGuard writeAccessGuard) {
        this(repository, writeAccessGuard, null);
    }

    public CompraService(CompraRepository repository, WriteAccessGuard writeAccessGuard, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
        this.auditoriaService = auditoriaService;
    }

    public List<Compra> recientes(String query) { return repository.findRecent(query, 150); }

    public List<CuentaPorPagar> cuentasPorPagarPendientes() {
        return repository.listarCuentasPorPagarPendientes(150);
    }

    public OperationResult<Compra> registrar(RegistroCompraSimple command) {
        OperationResult<Compra> blocked = bloquearSiNoPuedeEscribir("registrar compra");
        if (blocked != null) return blocked;
        try {
            validar(command);
            Compra compra = repository.registrarCompraSimple(command);
            auditar("REGISTRAR_COMPRA_SIMPLE", "Compra simple registrada.", compra.id());
            return OperationResult.success(compra, "Compra registrada, stock actualizado y movimiento creado.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar la compra. Revise producto, proveedor, cantidad y costo.");
        }
    }

    public OperationResult<Compra> registrarAvanzada(RegistroCompraAvanzada command) {
        OperationResult<Compra> blocked = bloquearSiNoPuedeEscribir("registrar compra avanzada");
        if (blocked != null) return blocked;
        try {
            validar(command);
            Compra compra = repository.registrarCompraAvanzada(command);
            auditar(command.compraCredito() ? "REGISTRAR_COMPRA_CREDITO" : "REGISTRAR_COMPRA_PAGADA", "Compra avanzada registrada.", compra.id());
            String mensaje = command.compraCredito()
                    ? "Compra a crédito registrada, stock actualizado y cuenta por pagar creada."
                    : "Compra pagada registrada y stock actualizado.";
            return OperationResult.success(compra, mensaje);
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar la compra avanzada. Revise proveedor, productos, cantidades y costos.");
        }
    }

    public OperationResult<CuentaPorPagar> registrarPagoProveedor(RegistroPagoProveedor command) {
        OperationResult<CuentaPorPagar> blocked = bloquearSiNoPuedeEscribir("registrar pago a proveedor");
        if (blocked != null) return blocked;
        try {
            validar(command);
            CuentaPorPagar cuenta = repository.registrarPagoProveedor(command);
            auditar("REGISTRAR_PAGO_PROVEEDOR", "Pago a proveedor registrado.", cuenta.id());
            return OperationResult.success(cuenta, cuenta.saldoPendiente().compareTo(BigDecimal.ZERO) == 0
                    ? "Pago registrado. La cuenta por pagar quedó saldada."
                    : "Pago registrado. La cuenta por pagar quedó parcialmente pagada.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar el pago a proveedor. Revise monto, cuenta pendiente y forma de pago.");
        }
    }

    private void validar(RegistroCompraSimple command) {
        if (command == null) throw new ValidationException("La compra no puede estar vacía.");
        if (command.productoId() == null) throw new ValidationException("Debe seleccionar un producto.");
        if (command.cantidad() == null || command.cantidad().compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("La cantidad debe ser mayor que cero.");
        if (command.costoUnitario() == null || command.costoUnitario().compareTo(BigDecimal.ZERO) < 0) throw new ValidationException("El costo unitario no puede ser negativo.");
    }

    private void validar(RegistroCompraAvanzada command) {
        if (command == null) throw new ValidationException("La compra no puede estar vacía.");
        if (command.detalles() == null || command.detalles().isEmpty()) throw new ValidationException("La compra debe tener al menos un producto.");
        for (DetalleCompraAvanzada detalle : command.detalles()) {
            if (detalle == null) throw new ValidationException("La compra contiene un detalle vacío.");
            if (detalle.productoId() == null) throw new ValidationException("Todos los detalles deben tener producto.");
            if (detalle.cantidad() == null || detalle.cantidad().compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("Cada cantidad debe ser mayor que cero.");
            if (detalle.costoUnitario() == null || detalle.costoUnitario().compareTo(BigDecimal.ZERO) < 0) throw new ValidationException("El costo unitario no puede ser negativo.");
        }
    }

    private void validar(RegistroPagoProveedor command) {
        if (command == null) throw new ValidationException("El pago no puede estar vacío.");
        if (command.cuentaPorPagarId() == null) throw new ValidationException("Debe seleccionar una cuenta por pagar.");
        if (command.monto() == null || command.monto().compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("El monto del pago debe ser mayor que cero.");
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }

    private void auditar(String accion, String resumen, Long entidadId) {
        if (auditoriaService != null) {
            auditoriaService.registrarExito(null, "Compras", accion, "compra", entidadId, resumen);
        }
    }
}
