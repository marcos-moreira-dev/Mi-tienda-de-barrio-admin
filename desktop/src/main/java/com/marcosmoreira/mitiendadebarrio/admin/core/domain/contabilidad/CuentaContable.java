package com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad;

/** Cuenta del plan contable básico local. */
public record CuentaContable(
        Long id,
        String codigo,
        String nombre,
        TipoCuentaContable tipo,
        Long cuentaPadreId,
        boolean imputable,
        boolean activa
) {}
