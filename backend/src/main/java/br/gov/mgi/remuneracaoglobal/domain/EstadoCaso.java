package br.gov.mgi.remuneracaoglobal.domain;

public enum EstadoCaso {
    RASCUNHO,
    PROPOSTA_ENVIADA,
    EM_ANALISE,
    DEVOLVIDO,
    APROVADO,
    CANCELADO;

    public boolean terminal() {
        return this == APROVADO || this == CANCELADO;
    }
}
