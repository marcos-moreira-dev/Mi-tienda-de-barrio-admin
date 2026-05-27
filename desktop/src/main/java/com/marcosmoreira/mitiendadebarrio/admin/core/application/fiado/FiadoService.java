package com.marcosmoreira.mitiendadebarrio.admin.core.application.fiado;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja.MetodoPagoCaja;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiado.ClienteFiado;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiado.CuentaPorCobrar;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiado.EstadoClienteFiado;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.math.BigDecimal;
import java.util.List;

/**
 * Casos de uso de fiado y cuentas por cobrar simples.
 *
 * <p>Este módulo es opcional para tiendas que trabajan con crédito informal.
 * No reemplaza contabilidad formal ni cartera financiera avanzada.</p>
 */
public final class FiadoService {

    private final FiadoRepository repository;
    private final WriteAccessGuard writeAccessGuard;

    public FiadoService(FiadoRepository repository) {
        this(repository, null);
    }

    public FiadoService(FiadoRepository repository, WriteAccessGuard writeAccessGuard) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
    }

    public List<ClienteFiado> buscarClientes(String query, boolean incluirInactivos) {
        return repository.findClientes(query == null ? "" : query.strip(), incluirInactivos);
    }

    public List<CuentaPorCobrar> cuentasAbiertas(Long clienteId) {
        return repository.findCuentasAbiertas(clienteId);
    }

    public OperationResult<ClienteFiado> guardarCliente(ClienteFiado cliente) {
        OperationResult<ClienteFiado> blocked = bloquearSiNoPuedeEscribir("guardar cliente de fiado");
        if (blocked != null) return blocked;
        try {
            ClienteFiado normalizado = normalizarCliente(cliente);
            ClienteFiado guardado = repository.guardarCliente(normalizado);
            return OperationResult.success(guardado, "Cliente de fiado guardado correctamente.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo guardar el cliente de fiado. Revise si los datos están completos.");
        }
    }

    public OperationResult<CuentaPorCobrar> abrirCuenta(Long clienteId, BigDecimal monto, String observacion) {
        OperationResult<CuentaPorCobrar> blocked = bloquearSiNoPuedeEscribir("abrir cuenta por cobrar");
        if (blocked != null) return blocked;
        try {
            if (clienteId == null) throw new ValidationException("Debe seleccionar un cliente de fiado.");
            BigDecimal montoSeguro = montoSeguro(monto, "El monto de la cuenta debe ser mayor que cero.");
            CuentaPorCobrar cuenta = repository.abrirCuenta(clienteId, montoSeguro, observacion);
            return OperationResult.success(cuenta, "Cuenta por cobrar abierta correctamente.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo abrir la cuenta por cobrar.");
        }
    }

    public OperationResult<Void> registrarAbono(Long cuentaId, BigDecimal monto, MetodoPagoCaja metodoPago, String observacion) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("registrar abono de fiado");
        if (blocked != null) return blocked;
        try {
            if (cuentaId == null) throw new ValidationException("Debe seleccionar una cuenta abierta.");
            BigDecimal montoSeguro = montoSeguro(monto, "El abono debe ser mayor que cero.");
            repository.registrarAbono(cuentaId, montoSeguro, metodoPago == null ? MetodoPagoCaja.EFECTIVO : metodoPago, observacion);
            return OperationResult.success(null, "Abono registrado correctamente.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar el abono. Revise el saldo pendiente de la cuenta.");
        }
    }

    public OperationResult<Void> desactivarCliente(Long clienteId) {
        return cambiarEstadoCliente(clienteId, false, "Cliente de fiado desactivado.", "desactivar cliente de fiado");
    }

    public OperationResult<Void> reactivarCliente(Long clienteId) {
        return cambiarEstadoCliente(clienteId, true, "Cliente de fiado reactivado.", "reactivar cliente de fiado");
    }

    private OperationResult<Void> cambiarEstadoCliente(Long clienteId, boolean activo, String mensaje, String operacion) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir(operacion);
        if (blocked != null) return blocked;
        try {
            if (clienteId == null) throw new ValidationException("Debe seleccionar un cliente de fiado.");
            repository.cambiarEstadoCliente(clienteId, activo);
            return OperationResult.success(null, mensaje);
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo cambiar el estado del cliente de fiado.");
        }
    }

    private ClienteFiado normalizarCliente(ClienteFiado cliente) {
        if (cliente == null) throw new ValidationException("El cliente de fiado no puede estar vacío.");
        if (cliente.nombre() == null || cliente.nombre().isBlank()) throw new ValidationException("El nombre del cliente es obligatorio.");

        BigDecimal limiteCredito = cliente.limiteCredito() == null ? BigDecimal.ZERO : cliente.limiteCredito();
        if (limiteCredito.compareTo(BigDecimal.ZERO) < 0) throw new ValidationException("El límite de crédito no puede ser negativo.");

        return new ClienteFiado(
                cliente.id(),
                cliente.nombre().strip(),
                cliente.telefono(),
                cliente.direccion(),
                limiteCredito,
                cliente.estado() == null ? EstadoClienteFiado.ACTIVO : cliente.estado(),
                cliente.observacion(),
                cliente.saldoPendiente() == null ? BigDecimal.ZERO : cliente.saldoPendiente()
        );
    }

    private BigDecimal montoSeguro(BigDecimal monto, String mensaje) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException(mensaje);
        return monto;
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }
}
