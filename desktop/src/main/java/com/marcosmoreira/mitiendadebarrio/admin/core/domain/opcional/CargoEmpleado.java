package com.marcosmoreira.mitiendadebarrio.admin.core.domain.opcional;

/** Cargo operativo mínimo para empleados locales. */
public record CargoEmpleado(Long id, String codigo, String nombre, String descripcion, String estado) {}
