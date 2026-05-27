package com.marcosmoreira.mitiendadebarrio.admin.core.application.caja;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Puerto de persistencia para caja diaria local. */
public interface CajaDiariaRepository {
    Optional<CajaDiaria> findByFecha(LocalDate fecha);
    List<CajaDiaria> findRecent(int limit);
    List<MovimientoCaja> findMovimientos(Long cajaDiariaId);
    List<TipoGasto> findTiposGastoActivos();
    List<GastoOperativo> findGastos(Long cajaDiariaId);
    List<ArqueoCaja> findArqueos(Long cajaDiariaId);
    CajaDiaria abrir(LocalDate fecha, BigDecimal saldoInicial, String observacion);
    MovimientoCaja registrarMovimiento(RegistroMovimientoCaja movimiento);
    GastoOperativo registrarGasto(RegistroGastoOperativo gasto);
    ArqueoCaja registrarArqueo(RegistroArqueoCaja arqueo);
    CajaDiaria cerrar(Long cajaDiariaId, BigDecimal saldoContado, String observacion);
}
