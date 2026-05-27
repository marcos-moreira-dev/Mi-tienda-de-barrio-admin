package com.marcosmoreira.mitiendadebarrio.admin.core.domain.opcional;

/** Registro básico de empleado local; no implementa nómina. */
public record EmpleadoLocal(
        Long id,
        Long cargoId,
        Long terceroId,
        String nombre,
        String identificacion,
        String telefono,
        String fechaIngreso,
        String estado,
        String observacion
) {}
