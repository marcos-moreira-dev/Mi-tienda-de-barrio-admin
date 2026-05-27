package com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja;

/** Tipo simple de movimiento de caja. */
public enum TipoMovimientoCaja {
    INGRESO("INGRESO", "Ingreso"), EGRESO("EGRESO", "Egreso"), AJUSTE("AJUSTE", "Ajuste");
    private final String dbValue; private final String label;
    TipoMovimientoCaja(String dbValue, String label){this.dbValue=dbValue;this.label=label;}
    public String dbValue(){return dbValue;} public String label(){return label;}
    public static TipoMovimientoCaja fromDb(String value){for(TipoMovimientoCaja t:values()) if(t.dbValue.equals(value)) return t; return INGRESO;}
}
