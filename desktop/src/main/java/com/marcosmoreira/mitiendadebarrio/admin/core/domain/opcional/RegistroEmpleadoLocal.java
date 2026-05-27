package com.marcosmoreira.mitiendadebarrio.admin.core.domain.opcional;

public record RegistroEmpleadoLocal(
        Long cargoId,
        Long terceroId,
        String nombre,
        String identificacion,
        String telefono,
        String fechaIngreso,
        String observacion
) {}
