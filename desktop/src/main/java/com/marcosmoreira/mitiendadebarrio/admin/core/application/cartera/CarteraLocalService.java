package com.marcosmoreira.mitiendadebarrio.admin.core.application.cartera;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.auditoria.AuditoriaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja.MetodoPagoCaja;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.cartera.CarteraCajaResultado;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.cartera.RegistroAbonoConCaja;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.cartera.RegistroPagoProveedorConCaja;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.cartera.RegistroVentaPagadaEnCaja;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.math.BigDecimal;

/**
 * Casos de uso que conectan cartera, ventas y cuentas por pagar con caja.
 *
 * <p>Regla central: cartera representa deuda pendiente; caja representa dinero real que entró o salió.</p>
 */
public final class CarteraLocalService {
    private final CarteraLocalRepository repository;
    private final WriteAccessGuard writeAccessGuard;
    private final AuditoriaService auditoriaService;

    public CarteraLocalService(CarteraLocalRepository repository) {
        this(repository, null, null);
    }

    public CarteraLocalService(CarteraLocalRepository repository, WriteAccessGuard writeAccessGuard, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
        this.auditoriaService = auditoriaService;
    }

    public OperationResult<CarteraCajaResultado> registrarAbonoConCaja(RegistroAbonoConCaja command) {
        OperationResult<CarteraCajaResultado> blocked = bloquearSiNoPuedeEscribir("registrar abono con caja");
        if (blocked != null) return blocked;
        try {
            validar(command);
            CarteraCajaResultado resultado = repository.registrarAbonoConCaja(command);
            auditar("REGISTRAR_ABONO_CON_CAJA", "Abono de fiado conectado con caja.", "abono", resultado.entidadId());
            return OperationResult.success(resultado, "Abono registrado y conectado como ingreso de caja.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar el abono conectado a caja. Revise cuenta, saldo y caja abierta.");
        }
    }

    public OperationResult<CarteraCajaResultado> registrarPagoProveedorConCaja(RegistroPagoProveedorConCaja command) {
        OperationResult<CarteraCajaResultado> blocked = bloquearSiNoPuedeEscribir("registrar pago a proveedor con caja");
        if (blocked != null) return blocked;
        try {
            validar(command);
            CarteraCajaResultado resultado = repository.registrarPagoProveedorConCaja(command);
            auditar("REGISTRAR_PAGO_PROVEEDOR_CON_CAJA", "Pago a proveedor conectado con caja.", "pago_proveedor", resultado.entidadId());
            return OperationResult.success(resultado, resultado.saldoPendiente().compareTo(BigDecimal.ZERO) == 0
                    ? "Pago registrado, egreso de caja creado y cuenta por pagar saldada."
                    : "Pago registrado, egreso de caja creado y cuenta por pagar parcialmente pagada.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar el pago a proveedor conectado a caja. Revise cuenta, monto y caja abierta.");
        }
    }

    public OperationResult<CarteraCajaResultado> registrarVentaPagadaEnCaja(RegistroVentaPagadaEnCaja command) {
        OperationResult<CarteraCajaResultado> blocked = bloquearSiNoPuedeEscribir("conectar venta pagada con caja");
        if (blocked != null) return blocked;
        try {
            validar(command);
            CarteraCajaResultado resultado = repository.registrarVentaPagadaEnCaja(command);
            auditar("CONECTAR_VENTA_PAGADA_CAJA", "Venta pagada conectada con caja.", "venta_pago", resultado.entidadId());
            return OperationResult.success(resultado, "Venta pagada conectada como ingreso de caja.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo conectar la venta pagada con caja. Revise venta, pago y caja abierta.");
        }
    }

    private void validar(RegistroAbonoConCaja command) {
        if (command == null) throw new ValidationException("El abono no puede estar vacío.");
        if (command.cuentaPorCobrarId() == null) throw new ValidationException("Debe seleccionar una cuenta por cobrar.");
        if (command.cajaDiariaId() == null) throw new ValidationException("Debe seleccionar una caja abierta.");
        validarMonto(command.monto(), "El abono debe ser mayor que cero.");
    }

    private void validar(RegistroPagoProveedorConCaja command) {
        if (command == null) throw new ValidationException("El pago a proveedor no puede estar vacío.");
        if (command.cuentaPorPagarId() == null) throw new ValidationException("Debe seleccionar una cuenta por pagar.");
        if (command.cajaDiariaId() == null) throw new ValidationException("Debe seleccionar una caja abierta.");
        validarMonto(command.monto(), "El pago a proveedor debe ser mayor que cero.");
    }

    private void validar(RegistroVentaPagadaEnCaja command) {
        if (command == null) throw new ValidationException("La conexión de venta con caja no puede estar vacía.");
        if (command.ventaInternaId() == null) throw new ValidationException("Debe seleccionar una venta interna.");
        if (command.cajaDiariaId() == null) throw new ValidationException("Debe seleccionar una caja abierta.");
    }

    private void validarMonto(BigDecimal monto, String mensaje) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException(mensaje);
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }

    private void auditar(String accion, String resumen, String entidad, Long entidadId) {
        if (auditoriaService != null) {
            auditoriaService.registrarExito(null, "Cartera", accion, entidad, entidadId, resumen);
        }
    }
}
