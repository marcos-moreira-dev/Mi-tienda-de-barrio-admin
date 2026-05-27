package com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja;

/** Estados operativos de una caja diaria local. */
public enum EstadoCajaDiaria {
    ABIERTA("ABIERTA", "Abierta"), CERRADA("CERRADA", "Cerrada"), ANULADA("ANULADA", "Anulada");
    private final String dbValue; private final String label;
    EstadoCajaDiaria(String dbValue, String label){this.dbValue=dbValue;this.label=label;}
    public String dbValue(){return dbValue;} public String label(){return label;}
    public static EstadoCajaDiaria fromDb(String value){for(EstadoCajaDiaria e:values()) if(e.dbValue.equals(value)) return e; return ABIERTA;}
}
