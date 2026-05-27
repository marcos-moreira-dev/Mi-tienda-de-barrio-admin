package com.marcosmoreira.mitiendadebarrio.admin.core.application.caja;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.auditoria.AuditoriaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja.*;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Casos de uso de caja diaria local. No reemplaza contabilidad formal. */
public final class CajaDiariaService {
    private final CajaDiariaRepository repository;
    private final WriteAccessGuard writeAccessGuard;
    private final AuditoriaService auditoriaService;

    public CajaDiariaService(CajaDiariaRepository repository) {
        this(repository, null, null);
    }

    public CajaDiariaService(CajaDiariaRepository repository, WriteAccessGuard writeAccessGuard) {
        this(repository, writeAccessGuard, null);
    }

    public CajaDiariaService(CajaDiariaRepository repository, WriteAccessGuard writeAccessGuard, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
        this.auditoriaService = auditoriaService;
    }

    public List<CajaDiaria> recientes() { return repository.findRecent(60); }
    public List<MovimientoCaja> movimientos(Long cajaDiariaId) { return repository.findMovimientos(cajaDiariaId); }
    public List<TipoGasto> tiposGastoActivos() { return repository.findTiposGastoActivos(); }
    public List<GastoOperativo> gastos(Long cajaDiariaId) { return repository.findGastos(cajaDiariaId); }
    public List<ArqueoCaja> arqueos(Long cajaDiariaId) { return repository.findArqueos(cajaDiariaId); }

    public OperationResult<CajaDiaria> abrirHoy(BigDecimal saldoInicial, String observacion) {
        OperationResult<CajaDiaria> blocked = bloquearSiNoPuedeEscribir("abrir caja diaria");
        if (blocked != null) return blocked;
        try {
            BigDecimal inicial = saldoInicial == null ? BigDecimal.ZERO : saldoInicial;
            if (inicial.compareTo(BigDecimal.ZERO) < 0) throw new ValidationException("El saldo inicial no puede ser negativo.");
            if (repository.findByFecha(LocalDate.now()).isPresent()) throw new ValidationException("Ya existe una caja registrada para el día de hoy.");
            CajaDiaria caja = repository.abrir(LocalDate.now(), inicial, observacion);
            auditar("ABRIR_CAJA", "caja_diaria", caja.id(), "Caja diaria abierta.");
            return OperationResult.success(caja, "Caja del día abierta correctamente.");
        } catch (ValidationException ex) { return OperationResult.failure(ex.getMessage()); }
        catch (RuntimeException ex) { return OperationResult.failure("No se pudo abrir la caja diaria."); }
    }

    public OperationResult<MovimientoCaja> registrarMovimiento(Long cajaId, TipoMovimientoCaja tipo, BigDecimal monto, MetodoPagoCaja metodo, String descripcion) {
        OperationResult<MovimientoCaja> blocked = bloquearSiNoPuedeEscribir("registrar movimiento de caja");
        if (blocked != null) return blocked;
        try {
            if (cajaId == null) throw new ValidationException("Debe seleccionar una caja abierta.");
            if (tipo == null || tipo == TipoMovimientoCaja.AJUSTE) throw new ValidationException("Use ingreso o egreso para esta operación simple.");
            if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("El monto debe ser mayor que cero.");
            if (descripcion == null || descripcion.isBlank()) throw new ValidationException("La descripción es obligatoria para trazabilidad humana.");
            MovimientoCaja movimiento = repository.registrarMovimiento(new RegistroMovimientoCaja(cajaId, tipo, monto, metodo == null ? MetodoPagoCaja.EFECTIVO : metodo, descripcion));
            auditar("REGISTRAR_MOVIMIENTO_CAJA", "movimiento_caja", movimiento.id(), "Movimiento de caja registrado.");
            return OperationResult.success(movimiento, "Movimiento de caja registrado.");
        } catch (ValidationException ex) { return OperationResult.failure(ex.getMessage()); }
        catch (RuntimeException ex) { return OperationResult.failure("No se pudo registrar el movimiento de caja."); }
    }

    public OperationResult<GastoOperativo> registrarGastoOperativo(Long cajaId, Long tipoGastoId, BigDecimal monto, MetodoPagoCaja formaPago, String descripcion, String referencia, String observacion) {
        OperationResult<GastoOperativo> blocked = bloquearSiNoPuedeEscribir("registrar gasto operativo");
        if (blocked != null) return blocked;
        try {
            if (cajaId == null) throw new ValidationException("Debe seleccionar una caja abierta.");
            if (tipoGastoId == null) throw new ValidationException("Debe seleccionar un tipo de gasto.");
            if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("El monto del gasto debe ser mayor que cero.");
            if (descripcion == null || descripcion.isBlank()) throw new ValidationException("La descripción del gasto es obligatoria.");
            GastoOperativo gasto = repository.registrarGasto(new RegistroGastoOperativo(cajaId, tipoGastoId, monto, formaPago == null ? MetodoPagoCaja.EFECTIVO : formaPago, descripcion, referencia, observacion));
            auditar("REGISTRAR_GASTO_OPERATIVO", "gasto_operativo", gasto.id(), "Gasto operativo registrado.");
            return OperationResult.success(gasto, "Gasto operativo registrado y descontado de caja.");
        } catch (ValidationException ex) { return OperationResult.failure(ex.getMessage()); }
        catch (RuntimeException ex) { return OperationResult.failure("No se pudo registrar el gasto operativo."); }
    }

    public OperationResult<ArqueoCaja> registrarArqueo(Long cajaId, BigDecimal saldoContado, String responsableTexto, String observacion) {
        OperationResult<ArqueoCaja> blocked = bloquearSiNoPuedeEscribir("registrar arqueo de caja");
        if (blocked != null) return blocked;
        try {
            if (cajaId == null) throw new ValidationException("Debe seleccionar una caja abierta.");
            if (saldoContado == null || saldoContado.compareTo(BigDecimal.ZERO) < 0) throw new ValidationException("El saldo contado no puede ser negativo.");
            ArqueoCaja arqueo = repository.registrarArqueo(new RegistroArqueoCaja(cajaId, saldoContado, responsableTexto, observacion));
            auditar("REGISTRAR_ARQUEO_CAJA", "arqueo_caja", arqueo.id(), "Arqueo de caja registrado.");
            return OperationResult.success(arqueo, "Arqueo de caja registrado.");
        } catch (ValidationException ex) { return OperationResult.failure(ex.getMessage()); }
        catch (RuntimeException ex) { return OperationResult.failure("No se pudo registrar el arqueo de caja."); }
    }

    public OperationResult<CajaDiaria> cerrar(Long cajaId, BigDecimal saldoContado, String observacion) {
        OperationResult<CajaDiaria> blocked = bloquearSiNoPuedeEscribir("cerrar caja diaria");
        if (blocked != null) return blocked;
        try {
            if (cajaId == null) throw new ValidationException("Debe seleccionar una caja para cerrar.");
            if (saldoContado == null || saldoContado.compareTo(BigDecimal.ZERO) < 0) throw new ValidationException("El saldo contado no puede ser negativo.");
            CajaDiaria caja = repository.cerrar(cajaId, saldoContado, observacion);
            auditar("CERRAR_CAJA", "caja_diaria", caja.id(), "Caja diaria cerrada.");
            return OperationResult.success(caja, "Caja diaria cerrada. Revise diferencia y haga respaldo del día.");
        } catch (ValidationException ex) { return OperationResult.failure(ex.getMessage()); }
        catch (RuntimeException ex) { return OperationResult.failure("No se pudo cerrar la caja diaria."); }
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }

    private void auditar(String accion, String entidad, Long entidadId, String resumen) {
        if (auditoriaService != null) {
            auditoriaService.registrarExito(null, "Caja", accion, entidad, entidadId, resumen);
        }
    }
}
