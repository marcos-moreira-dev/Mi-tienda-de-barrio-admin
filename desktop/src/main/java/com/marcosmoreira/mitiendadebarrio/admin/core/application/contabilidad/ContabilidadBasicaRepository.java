package com.marcosmoreira.mitiendadebarrio.admin.core.application.contabilidad;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.AsientoContable;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.CrearAsientoContableSolicitud;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.CuentaContable;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.PlantillaAsiento;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.ReglaContableEvento;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad.TipoDiarioContable;

import java.util.List;
import java.util.Optional;

/** Puerto local para contabilidad básica SQLite. */
public interface ContabilidadBasicaRepository {
    List<CuentaContable> listarCuentasActivas();
    List<TipoDiarioContable> listarTiposDiarioActivos();
    List<PlantillaAsiento> listarPlantillasActivas();
    List<ReglaContableEvento> listarReglasActivas();
    Optional<PlantillaAsiento> buscarPlantillaPorCodigo(String codigo);
    Optional<ReglaContableEvento> buscarReglaActivaPorEvento(String eventoCodigo);
    Optional<AsientoContable> buscarAsientoPorId(long id);
    List<AsientoContable> listarAsientosRecientes(int limite);
    AsientoContable registrarAsiento(CrearAsientoContableSolicitud solicitud);
    void anularAsiento(long asientoId, String motivo);
}
