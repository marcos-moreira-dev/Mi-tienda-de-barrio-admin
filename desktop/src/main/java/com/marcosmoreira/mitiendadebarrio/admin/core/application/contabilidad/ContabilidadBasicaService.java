package com.marcosmoreira.mitiendadebarrio.admin.core.application.contabilidad;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.auditoria.AuditoriaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.AsientoContable;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.AsientoContableDetalleSolicitud;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.CrearAsientoContableSolicitud;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.CrearAsientoDesdePlantillaSolicitud;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.CuentaContable;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.LadoPlantillaAsiento;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.PlantillaAsiento;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.PlantillaAsientoDetalle;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.ReglaContableEvento;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.TipoDiarioContable;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Casos de uso de contabilidad básica local.
 * No reemplaza revisión profesional ni obligaciones contables/tributarias formales.
 */
public final class ContabilidadBasicaService {
    private static final String MODULO = "Contabilidad básica";

    private final ContabilidadBasicaRepository repository;
    private final WriteAccessGuard writeAccessGuard;
    private final AuditoriaService auditoriaService;

    public ContabilidadBasicaService(ContabilidadBasicaRepository repository) {
        this(repository, null, null);
    }

    public ContabilidadBasicaService(
            ContabilidadBasicaRepository repository,
            WriteAccessGuard writeAccessGuard,
            AuditoriaService auditoriaService
    ) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
        this.auditoriaService = auditoriaService;
    }

    public List<CuentaContable> listarCuentasActivas() {
        return repository.listarCuentasActivas();
    }

    public List<TipoDiarioContable> listarTiposDiarioActivos() {
        return repository.listarTiposDiarioActivos();
    }

    public List<PlantillaAsiento> listarPlantillasActivas() {
        return repository.listarPlantillasActivas();
    }

    public List<ReglaContableEvento> listarReglasActivas() {
        return repository.listarReglasActivas();
    }

    public List<AsientoContable> listarAsientosRecientes(int limite) {
        int limiteSeguro = limite <= 0 ? 50 : Math.min(limite, 300);
        return repository.listarAsientosRecientes(limiteSeguro);
    }

    public Optional<AsientoContable> buscarAsientoPorId(long id) {
        return repository.buscarAsientoPorId(id);
    }

    public OperationResult<AsientoContable> registrarAsiento(CrearAsientoContableSolicitud solicitud) {
        OperationResult<AsientoContable> blocked = bloquearSiNoPuedeEscribir("registrar asiento contable");
        if (blocked != null) return blocked;
        try {
            validarSolicitud(solicitud);
            AsientoContable asiento = repository.registrarAsiento(solicitud);
            auditar("REGISTRAR_ASIENTO", asiento.id(), "Asiento contable registrado: " + asiento.numeroAsiento());
            return OperationResult.success(asiento, "Asiento contable registrado correctamente.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar el asiento contable. Revise cuentas, importes y cuadre debe/haber.");
        }
    }

    public OperationResult<AsientoContable> registrarAsientoDesdePlantilla(CrearAsientoDesdePlantillaSolicitud solicitud) {
        OperationResult<AsientoContable> blocked = bloquearSiNoPuedeEscribir("registrar asiento desde plantilla");
        if (blocked != null) return blocked;
        try {
            validarSolicitudPlantilla(solicitud);
            PlantillaAsiento plantilla = resolverPlantilla(solicitud);
            validarPlantillaSimple(plantilla);
            BigDecimal importe = zeroIfNull(solicitud.importe());
            List<AsientoContableDetalleSolicitud> detalles = new ArrayList<>();
            for (PlantillaAsientoDetalle detalle : plantilla.detalles()) {
                BigDecimal debe = detalle.lado() == LadoPlantillaAsiento.DEBE ? importe : BigDecimal.ZERO;
                BigDecimal haber = detalle.lado() == LadoPlantillaAsiento.HABER ? importe : BigDecimal.ZERO;
                detalles.add(new AsientoContableDetalleSolicitud(
                        detalle.cuentaId(),
                        detalle.descripcion(),
                        debe,
                        haber
                ));
            }
            CrearAsientoContableSolicitud asientoSolicitud = new CrearAsientoContableSolicitud(
                    tipoDiarioPorEvento(solicitud.eventoCodigo()),
                    solicitud.fechaAsiento(),
                    solicitud.concepto(),
                    solicitud.origenTipo(),
                    solicitud.origenId(),
                    detalles
            );
            validarSolicitud(asientoSolicitud);
            AsientoContable asiento = repository.registrarAsiento(asientoSolicitud);
            auditar("REGISTRAR_ASIENTO_PLANTILLA", asiento.id(),
                    "Asiento desde plantilla " + plantilla.codigo() + " registrado: " + asiento.numeroAsiento());
            return OperationResult.success(asiento, "Asiento contable generado desde plantilla correctamente.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        } catch (RuntimeException ex) {
            return OperationResult.failure("No se pudo registrar el asiento desde plantilla. Revise plantilla, evento e importe.");
        }
    }

    public OperationResult<Void> anularAsiento(long asientoId, String motivo) {
        OperationResult<Void> blocked = bloquearSiNoPuedeEscribir("anular asiento contable");
        if (blocked != null) return blocked;
        if (asientoId <= 0) {
            return OperationResult.failure("Debe indicar el asiento contable a anular.");
        }
        if (motivo == null || motivo.isBlank()) {
            return OperationResult.failure("Debe indicar un motivo para anular el asiento contable.");
        }
        repository.anularAsiento(asientoId, motivo.strip());
        auditar("ANULAR_ASIENTO", asientoId, "Asiento contable anulado. Motivo: " + motivo.strip());
        return OperationResult.success(null, "Asiento contable anulado.");
    }

    private void validarSolicitudPlantilla(CrearAsientoDesdePlantillaSolicitud solicitud) {
        if (solicitud == null) {
            throw new ValidationException("Debe indicar los datos para generar el asiento desde plantilla.");
        }
        boolean tienePlantilla = solicitud.plantillaCodigo() != null && !solicitud.plantillaCodigo().isBlank();
        boolean tieneEvento = solicitud.eventoCodigo() != null && !solicitud.eventoCodigo().isBlank();
        if (!tienePlantilla && !tieneEvento) {
            throw new ValidationException("Debe indicar una plantilla o un evento contable.");
        }
        if (solicitud.concepto() == null || solicitud.concepto().isBlank()) {
            throw new ValidationException("Debe indicar el concepto del asiento contable.");
        }
        if (zeroIfNull(solicitud.importe()).compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("El importe del asiento desde plantilla debe ser mayor que cero.");
        }
    }

    private PlantillaAsiento resolverPlantilla(CrearAsientoDesdePlantillaSolicitud solicitud) {
        if (solicitud.plantillaCodigo() != null && !solicitud.plantillaCodigo().isBlank()) {
            return repository.buscarPlantillaPorCodigo(solicitud.plantillaCodigo())
                    .orElseThrow(() -> new ValidationException("No existe la plantilla contable indicada."));
        }
        ReglaContableEvento regla = repository.buscarReglaActivaPorEvento(solicitud.eventoCodigo())
                .orElseThrow(() -> new ValidationException("No existe una regla contable activa para el evento indicado."));
        if (regla.plantillaId() == null) {
            throw new ValidationException("La regla contable no tiene plantilla asociada.");
        }
        return repository.listarPlantillasActivas().stream()
                .filter(plantilla -> regla.plantillaId().equals(plantilla.id()))
                .findFirst()
                .orElseThrow(() -> new ValidationException("La plantilla asociada a la regla no está activa."));
    }

    private void validarPlantillaSimple(PlantillaAsiento plantilla) {
        if (plantilla == null || plantilla.detalles() == null || plantilla.detalles().size() < 2) {
            throw new ValidationException("La plantilla contable debe tener al menos dos líneas.");
        }
        long debe = plantilla.detalles().stream().filter(detalle -> detalle.lado() == LadoPlantillaAsiento.DEBE).count();
        long haber = plantilla.detalles().stream().filter(detalle -> detalle.lado() == LadoPlantillaAsiento.HABER).count();
        if (debe != 1 || haber != 1) {
            throw new ValidationException("Por ahora solo se permiten plantillas simples con una línea en debe y una línea en haber.");
        }
    }

    private String tipoDiarioPorEvento(String eventoCodigo) {
        if (eventoCodigo == null) {
            return "GENERAL";
        }
        String evento = eventoCodigo.strip().toUpperCase();
        if (evento.contains("VENTA")) return "VENTAS";
        if (evento.contains("COMPRA") || evento.contains("PROVEEDOR")) return "COMPRAS";
        if (evento.contains("ABONO") || evento.contains("CARTERA")) return "CARTERA";
        if (evento.contains("GASTO") || evento.contains("CAJA") || evento.contains("PAGO")) return "CAJA";
        if (evento.contains("MERMA") || evento.contains("AJUSTE")) return "AJUSTES";
        return "GENERAL";
    }

    private void validarSolicitud(CrearAsientoContableSolicitud solicitud) {
        if (solicitud == null) {
            throw new ValidationException("Debe indicar los datos del asiento contable.");
        }
        if (solicitud.concepto() == null || solicitud.concepto().isBlank()) {
            throw new ValidationException("Debe indicar el concepto del asiento contable.");
        }
        if (solicitud.detalles() == null || solicitud.detalles().size() < 2) {
            throw new ValidationException("El asiento debe tener al menos dos líneas.");
        }
        BigDecimal totalDebe = BigDecimal.ZERO;
        BigDecimal totalHaber = BigDecimal.ZERO;
        int linea = 0;
        for (AsientoContableDetalleSolicitud detalle : solicitud.detalles()) {
            linea++;
            if (detalle == null || detalle.cuentaId() == null || detalle.cuentaId() <= 0) {
                throw new ValidationException("Cada línea del asiento debe indicar una cuenta contable.");
            }
            BigDecimal debe = zeroIfNull(detalle.debe());
            BigDecimal haber = zeroIfNull(detalle.haber());
            if (debe.compareTo(BigDecimal.ZERO) < 0 || haber.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("Los importes del asiento no pueden ser negativos.");
            }
            boolean tieneDebe = debe.compareTo(BigDecimal.ZERO) > 0;
            boolean tieneHaber = haber.compareTo(BigDecimal.ZERO) > 0;
            if (tieneDebe == tieneHaber) {
                throw new ValidationException("La línea " + linea + " debe tener valor en debe o en haber, pero no ambos.");
            }
            totalDebe = totalDebe.add(debe);
            totalHaber = totalHaber.add(haber);
        }
        if (totalDebe.compareTo(BigDecimal.ZERO) <= 0 || totalHaber.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("El asiento debe tener valores positivos en debe y haber.");
        }
        if (totalDebe.compareTo(totalHaber) != 0) {
            throw new ValidationException("El asiento no cuadra: debe y haber deben ser iguales.");
        }
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }

    private void auditar(String accion, Long entidadId, String resumen) {
        if (auditoriaService == null) return;
        auditoriaService.registrarExito(null, MODULO, accion, "asiento_contable", entidadId, resumen);
    }
}
