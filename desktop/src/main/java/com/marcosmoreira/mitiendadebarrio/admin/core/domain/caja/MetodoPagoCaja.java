package com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja;

/** Métodos de pago simples para caja local. */
public enum MetodoPagoCaja {
    EFECTIVO("EFECTIVO", "Efectivo"), TRANSFERENCIA("TRANSFERENCIA", "Transferencia"), TARJETA("TARJETA", "Tarjeta"), OTRO("OTRO", "Otro");
    private final String dbValue; private final String label;
    MetodoPagoCaja(String dbValue, String label){this.dbValue=dbValue;this.label=label;}
    public String dbValue(){return dbValue;} public String label(){return label;}
    public static MetodoPagoCaja fromDb(String value){for(MetodoPagoCaja m:values()) if(m.dbValue.equals(value)) return m; return EFECTIVO;}
}
