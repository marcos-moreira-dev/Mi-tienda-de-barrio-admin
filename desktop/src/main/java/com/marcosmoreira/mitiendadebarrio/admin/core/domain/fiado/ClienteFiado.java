package com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiado;
import java.math.BigDecimal;
public record ClienteFiado(Long id, String nombre, String telefono, String direccion, BigDecimal limiteCredito, EstadoClienteFiado estado, String observacion, BigDecimal saldoPendiente) { }
