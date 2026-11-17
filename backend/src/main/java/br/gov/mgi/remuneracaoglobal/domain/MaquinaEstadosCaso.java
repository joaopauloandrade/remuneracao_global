package br.gov.mgi.remuneracaoglobal.domain;

import static br.gov.mgi.remuneracaoglobal.domain.AcaoCaso.APROVAR;
import static br.gov.mgi.remuneracaoglobal.domain.AcaoCaso.ASSUMIR_ANALISE;
import static br.gov.mgi.remuneracaoglobal.domain.AcaoCaso.DEVOLVER;
import static br.gov.mgi.remuneracaoglobal.domain.AcaoCaso.ENVIAR;
import static br.gov.mgi.remuneracaoglobal.domain.EstadoCaso.APROVADO;
import static br.gov.mgi.remuneracaoglobal.domain.EstadoCaso.CANCELADO;
import static br.gov.mgi.remuneracaoglobal.domain.EstadoCaso.DEVOLVIDO;
import static br.gov.mgi.remuneracaoglobal.domain.EstadoCaso.EM_ANALISE;
import static br.gov.mgi.remuneracaoglobal.domain.EstadoCaso.PROPOSTA_ENVIADA;
import static br.gov.mgi.remuneracaoglobal.domain.EstadoCaso.RASCUNHO;

import java.util.Optional;

/**
 * Máquina de estados do caso (SPEC §4.1 e §4.2).
 * RMG: RASCUNHO → PROPOSTA_ENVIADA → EM_ANALISE → (DEVOLVIDO | APROVADO); DEVOLVIDO → PROPOSTA_ENVIADA.
 * RMGS: RASCUNHO → PROPOSTA_ENVIADA → APROVADO (sem análise técnica).
 * CANCELADO é alcançável a partir de qualquer estado não terminal.
 */
public final class MaquinaEstadosCaso {

    private MaquinaEstadosCaso() {
    }

    public static EstadoCaso proximoEstado(CasoTipo tipo, EstadoCaso atual, AcaoCaso acao) {
        return transicao(tipo, atual, acao).orElseThrow(() -> new RegraNegocioException(
                "Ação " + acao + " não permitida no estado " + atual + " para o tipo " + tipo + "."));
    }

    public static boolean podeEditarLinhas(EstadoCaso estado) {
        return estado == RASCUNHO || estado == DEVOLVIDO || estado == EM_ANALISE;
    }

    private static Optional<EstadoCaso> transicao(CasoTipo tipo, EstadoCaso atual, AcaoCaso acao) {
        if (atual.terminal()) {
            return Optional.empty();
        }
        boolean rmg = tipo == CasoTipo.RMG;
        return switch (acao) {
            case CANCELAR -> Optional.of(CANCELADO);
            case ENVIAR -> (atual == RASCUNHO || atual == DEVOLVIDO) ? Optional.of(PROPOSTA_ENVIADA) : Optional.empty();
            case ASSUMIR_ANALISE -> (rmg && atual == PROPOSTA_ENVIADA) ? Optional.of(EM_ANALISE) : Optional.empty();
            case DEVOLVER -> (rmg && atual == EM_ANALISE) ? Optional.of(DEVOLVIDO) : Optional.empty();
            case APROVAR -> ((rmg && atual == EM_ANALISE) || (!rmg && atual == PROPOSTA_ENVIADA))
                    ? Optional.of(APROVADO) : Optional.empty();
        };
    }
}
