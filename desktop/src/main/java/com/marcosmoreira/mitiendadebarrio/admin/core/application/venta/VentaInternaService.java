package com.marcosmoreira.mitiendadebarrio.admin.core.application.venta;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.auditoria.AuditoriaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.AnulacionVentaInterna;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.DetalleVentaInternaAvanzada;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.MetodoPagoVentaInterna;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.RegistroAnulacionVentaInterna;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.RegistroVentaInternaAvanzada;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.RegistroVentaInternaSimple;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta.VentaInterna;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.math.BigDecimal;
import java.util.List;

/** Casos de uso locales para ventas internas no tributarias. */
public final class VentaInternaService {
    private final VentaInternaRepository repository;
    private final WriteAccessGuard writeAccessGuard;
    private final AuditoriaService auditoriaService;

    public VentaInternaService(VentaInternaRepository repository) {
        this(repository, null, null);
    }

    public VentaInternaService(VentaInternaRepository repository, WriteAccessGuard writeAccessGuard) {
        this(repository, writeAccessGuard, null);
    }

    public VentaInternaService(VentaInternaRepository repository, WriteAccessGuard writeAccessGuard, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
        this.auditoriaService = auditoriaService;
    }

    public List<VentaInterna> recientes(String query) { return repository.findRecent(query, 150); }

    public OperationResult<VentaInterna> registrar(RegistroVentaInternaSimple command) {
        OperationResult<VentaInterna> blocked = bloquearSiNoPuedeEscribir("registrar venta interna");
        if (blocked != null) return blocked;
        try {
            validar(command);
            VentaInterna venta = repository.registrarVentaSimple(command);
            auditar("REGISTRAR_VENTA_SIMPLE", "Venta interna simple registrada.", venta.id());
            return OperationResult.success(venta, "Venta interna registrada y stock descontado.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar la venta interna. Revise stock disponible, cantidad y precio.");
        }
    }

    public OperationResult<VentaInterna> registrarAvanzada(RegistroVentaInternaAvanzada command) {
        OperationResult<VentaInterna> blocked = bloquearSiNoPuedeEscribir("registrar venta interna avanzada");
        if (blocked != null) return blocked;
        try {
            validar(command);
            VentaInterna venta = repository.registrarVentaAvanzada(command);
            auditar(command.esFiada() ? "REGISTRAR_VENTA_FIADA" : "REGISTRAR_VENTA_PAGADA", "Venta interna avanzada registrada.", venta.id());
            return OperationResult.success(venta, command.esFiada()
                    ? "Venta fiada registrada, stock descontado y cuenta por cobrar creada."
                    : "Venta pagada registrada, stock descontado y pago registrado.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar la venta avanzada. Revise cliente, stock, cantidades y precios.");
        }
    }

    public OperationResult<AnulacionVentaInterna> anular(RegistroAnulacionVentaInterna command) {
        OperationResult<AnulacionVentaInterna> blocked = bloquearSiNoPuedeEscribir("anular venta interna");
        if (blocked != null) return blocked;
        try {
            validar(command);
            AnulacionVentaInterna anulacion = repository.anularVenta(command);
            auditar("ANULAR_VENTA_INTERNA", "Venta interna anulada con reverso de stock.", command.ventaInternaId());
            return OperationResult.success(anulacion, "Venta interna anulada y stock reversado.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo anular la venta interna. Revise estado, abonos relacionados y stock.");
        }
    }

    private void validar(RegistroVentaInternaSimple command) {
        if (command == null) throw new ValidationException("La venta interna no puede estar vacía.");
        if (!command.advertenciaTributariaAceptada()) throw new ValidationException("Debe aceptar que esta venta interna no reemplaza facturación ni comprobantes oficiales.");
        if (command.productoId() == null) throw new ValidationException("Debe seleccionar un producto.");
        if (command.cantidad() == null || command.cantidad().compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("La cantidad debe ser mayor que cero.");
        if (command.precioUnitario() == null || command.precioUnitario().compareTo(BigDecimal.ZERO) < 0) throw new ValidationException("El precio unitario no puede ser negativo.");
    }

    private void validar(RegistroVentaInternaAvanzada command) {
        if (command == null) throw new ValidationException("La venta interna no puede estar vacía.");
        if (!command.advertenciaTributariaAceptada()) throw new ValidationException("Debe aceptar que esta venta interna no reemplaza facturación ni comprobantes oficiales.");
        if (command.detalles() == null || command.detalles().isEmpty()) throw new ValidationException("La venta debe tener al menos un producto.");
        MetodoPagoVentaInterna metodo = command.metodoPago() == null ? MetodoPagoVentaInterna.EFECTIVO : command.metodoPago();
        if (metodo == MetodoPagoVentaInterna.FIADO && command.clienteFiadoId() == null) {
            throw new ValidationException("Debe seleccionar un cliente para registrar una venta fiada.");
        }
        for (DetalleVentaInternaAvanzada detalle : command.detalles()) {
            if (detalle == null) throw new ValidationException("La venta contiene un detalle vacío.");
            if (detalle.productoId() == null) throw new ValidationException("Todos los detalles deben tener producto.");
            if (detalle.cantidad() == null || detalle.cantidad().compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("Cada cantidad debe ser mayor que cero.");
            if (detalle.precioUnitario() == null || detalle.precioUnitario().compareTo(BigDecimal.ZERO) < 0) throw new ValidationException("El precio unitario no puede ser negativo.");
        }
    }

    private void validar(RegistroAnulacionVentaInterna command) {
        if (command == null) throw new ValidationException("La anulación no puede estar vacía.");
        if (command.ventaInternaId() == null) throw new ValidationException("Debe seleccionar la venta interna a anular.");
        if (command.motivo() == null || command.motivo().isBlank()) throw new ValidationException("Debe escribir el motivo de anulación.");
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }

    private void auditar(String accion, String resumen, Long entidadId) {
        if (auditoriaService != null) {
            auditoriaService.registrarExito(null, "Ventas internas", accion, "venta_interna", entidadId, resumen);
        }
    }
}
