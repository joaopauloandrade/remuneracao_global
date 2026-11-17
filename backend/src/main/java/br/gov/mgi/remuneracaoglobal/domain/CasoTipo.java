package br.gov.mgi.remuneracaoglobal.domain;

public enum CasoTipo {
    RMG("RMG"),
    RMGS("RMGS");

    private final String prefixo;

    CasoTipo(String prefixo) {
        this.prefixo = prefixo;
    }

    public String prefixo() {
        return prefixo;
    }
}
