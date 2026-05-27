package com.marcosmoreira.mitiendadebarrio.admin.core.domain.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Resumen operativo para la pantalla de inicio. */
public record DashboardResumen(
        long productosActivos,
        long productosBajoStock,
        long productosAgotados,
        long productosPorComprar,
        long productosProximosVencer,
        BigDecimal ventasHoy,
        BigDecimal comprasHoy,
        boolean cajaAbierta,
        LocalDateTime ultimoRespaldo,
        String estadoLicencia
) {
    public BigDecimal ventasHoySeguro() { return ventasHoy == null ? BigDecimal.ZERO : ventasHoy; }
    public BigDecimal comprasHoySeguro() { return comprasHoy == null ? BigDecimal.ZERO : comprasHoy; }
}
