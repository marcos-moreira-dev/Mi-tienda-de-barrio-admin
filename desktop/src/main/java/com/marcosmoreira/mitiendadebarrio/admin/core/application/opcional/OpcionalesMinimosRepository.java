package com.marcosmoreira.mitiendadebarrio.admin.core.application.opcional;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.opcional.*;

import java.util.List;

/** Puerto local para módulos opcionales mínimos del ERP local. */
public interface OpcionalesMinimosRepository {
    List<TipoActivoNegocio> listarTiposActivoActivos();
    ActivoNegocio registrarActivo(RegistroActivoNegocio registro);
    List<ActivoNegocio> listarActivosRecientes(int limite);

    List<CargoEmpleado> listarCargosActivos();
    EmpleadoLocal registrarEmpleado(RegistroEmpleadoLocal registro);
    List<EmpleadoLocal> listarEmpleadosActivos();

    List<IndicadorOperativo> listarIndicadoresActivos();
    List<PlantillaImportacion> listarPlantillasImportacionActivas();
    LoteImportacion registrarLoteImportacion(RegistroLoteImportacion registro);
    void registrarErrorImportacion(long loteImportacionId, int numeroFila, String campo, String valorOriginal, String mensaje, String severidad);
    List<ChecklistOperativo> listarChecklistsActivos();
}
