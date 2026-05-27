package com.marcosmoreira.mitiendadebarrio.admin.core.application.opcional;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.auditoria.AuditoriaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.opcional.*;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.math.BigDecimal;
import java.util.List;

/** Casos de uso mínimos para módulos opcionales: activos, empleados, indicadores, importaciones y checklist. */
public final class OpcionalesMinimosService {
    private static final String MODULO = "Opcionales mínimos";

    private final OpcionalesMinimosRepository repository;
    private final WriteAccessGuard writeAccessGuard;
    private final AuditoriaService auditoriaService;

    public OpcionalesMinimosService(OpcionalesMinimosRepository repository) {
        this(repository, null, null);
    }

    public OpcionalesMinimosService(OpcionalesMinimosRepository repository, WriteAccessGuard writeAccessGuard, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
        this.auditoriaService = auditoriaService;
    }

    public List<TipoActivoNegocio> listarTiposActivoActivos() { return repository.listarTiposActivoActivos(); }
    public List<ActivoNegocio> listarActivosRecientes(int limite) { return repository.listarActivosRecientes(limite <= 0 ? 50 : Math.min(limite, 300)); }
    public List<CargoEmpleado> listarCargosActivos() { return repository.listarCargosActivos(); }
    public List<EmpleadoLocal> listarEmpleadosActivos() { return repository.listarEmpleadosActivos(); }
    public List<IndicadorOperativo> listarIndicadoresActivos() { return repository.listarIndicadoresActivos(); }
    public List<PlantillaImportacion> listarPlantillasImportacionActivas() { return repository.listarPlantillasImportacionActivas(); }
    public List<ChecklistOperativo> listarChecklistsActivos() { return repository.listarChecklistsActivos(); }

    public OperationResult<ActivoNegocio> registrarActivo(RegistroActivoNegocio registro) {
        OperationResult<ActivoNegocio> blocked = bloquearSiNoPuedeEscribir("registrar activo del negocio");
        if (blocked != null) return blocked;
        if (registro == null || registro.tipoActivoId() == null || registro.tipoActivoId() <= 0) return OperationResult.failure("Debe indicar el tipo de activo.");
        if (registro.nombre() == null || registro.nombre().isBlank()) return OperationResult.failure("Debe indicar el nombre del activo.");
        if (registro.valorEstimado() != null && registro.valorEstimado().compareTo(BigDecimal.ZERO) < 0) return OperationResult.failure("El valor estimado no puede ser negativo.");
        try {
            ActivoNegocio activo = repository.registrarActivo(registro);
            auditar("REGISTRAR_ACTIVO", "activo_negocio", activo.id(), "Activo registrado: " + activo.nombre());
            return OperationResult.success(activo, "Activo registrado correctamente.");
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar el activo del negocio.");
        }
    }

    public OperationResult<EmpleadoLocal> registrarEmpleado(RegistroEmpleadoLocal registro) {
        OperationResult<EmpleadoLocal> blocked = bloquearSiNoPuedeEscribir("registrar empleado local");
        if (blocked != null) return blocked;
        if (registro == null || registro.nombre() == null || registro.nombre().isBlank()) return OperationResult.failure("Debe indicar el nombre del empleado.");
        try {
            EmpleadoLocal empleado = repository.registrarEmpleado(registro);
            auditar("REGISTRAR_EMPLEADO", "empleado_local", empleado.id(), "Empleado registrado: " + empleado.nombre());
            return OperationResult.success(empleado, "Empleado registrado correctamente.");
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar el empleado local.");
        }
    }

    public OperationResult<LoteImportacion> registrarLoteImportacion(RegistroLoteImportacion registro) {
        OperationResult<LoteImportacion> blocked = bloquearSiNoPuedeEscribir("registrar lote de importación");
        if (blocked != null) return blocked;
        if (registro == null || registro.tipoImportacion() == null || registro.tipoImportacion().isBlank()) return OperationResult.failure("Debe indicar el tipo de importación.");
        if (registro.archivoOrigen() == null || registro.archivoOrigen().isBlank()) return OperationResult.failure("Debe indicar el archivo de origen.");
        try {
            LoteImportacion lote = repository.registrarLoteImportacion(registro);
            auditar("REGISTRAR_LOTE_IMPORTACION", "lote_importacion", lote.id(), "Lote de importación registrado: " + lote.tipoImportacion());
            return OperationResult.success(lote, "Lote de importación registrado correctamente.");
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar el lote de importación.");
        }
    }

    public OperationResult<Void> registrarErrorImportacion(long loteImportacionId, int numeroFila, String campo, String valorOriginal, String mensaje, String severidad) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("registrar error de importación");
        if (blocked != null) return blocked;
        if (loteImportacionId <= 0) return OperationResult.failure("Debe indicar el lote de importación.");
        if (numeroFila <= 0) return OperationResult.failure("Debe indicar una fila válida.");
        if (mensaje == null || mensaje.isBlank()) return OperationResult.failure("Debe indicar el mensaje del error de importación.");
        try {
            repository.registrarErrorImportacion(loteImportacionId, numeroFila, campo, valorOriginal, mensaje, severidad == null || severidad.isBlank() ? "ERROR" : severidad);
            auditar("REGISTRAR_ERROR_IMPORTACION", "lote_importacion", loteImportacionId, "Error de importación registrado en fila " + numeroFila);
            return OperationResult.success(null, "Error de importación registrado.");
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar el error de importación.");
        }
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard == null) return null;
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }

    private void auditar(String accion, String entidad, Long entidadId, String resumen) {
        if (auditoriaService != null) {
            auditoriaService.registrarExito(null, MODULO, accion, entidad, entidadId, resumen);
        }
    }
}
