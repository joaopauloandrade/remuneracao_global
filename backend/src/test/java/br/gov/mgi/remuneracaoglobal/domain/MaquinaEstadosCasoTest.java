package br.gov.mgi.remuneracaoglobal.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MaquinaEstadosCasoTest {

    @Test
    void rmgSeguePipelineCompletoAteAprovacao() {
        var estado = EstadoCaso.RASCUNHO;
        estado = MaquinaEstadosCaso.proximoEstado(CasoTipo.RMG, estado, AcaoCaso.ENVIAR);
        estado = MaquinaEstadosCaso.proximoEstado(CasoTipo.RMG, estado, AcaoCaso.ASSUMIR_ANALISE);
        estado = MaquinaEstadosCaso.proximoEstado(CasoTipo.RMG, estado, AcaoCaso.APROVAR);

        assertThat(estado).isEqualTo(EstadoCaso.APROVADO);
    }

    @Test
    void devolucaoPermiteReenvio() {
        var estado = MaquinaEstadosCaso.proximoEstado(CasoTipo.RMG, EstadoCaso.EM_ANALISE, AcaoCaso.DEVOLVER);
        assertThat(estado).isEqualTo(EstadoCaso.DEVOLVIDO);
        assertThat(MaquinaEstadosCaso.proximoEstado(CasoTipo.RMG, estado, AcaoCaso.ENVIAR))
                .isEqualTo(EstadoCaso.PROPOSTA_ENVIADA);
    }

    @Test
    void rmgsAprovaDiretamenteAposEnvio() {
        assertThat(MaquinaEstadosCaso.proximoEstado(CasoTipo.RMGS, EstadoCaso.PROPOSTA_ENVIADA, AcaoCaso.APROVAR))
                .isEqualTo(EstadoCaso.APROVADO);
    }

    @Test
    void rmgsNaoAceitaAnaliseTecnica() {
        assertThatThrownBy(() -> MaquinaEstadosCaso.proximoEstado(
                CasoTipo.RMGS, EstadoCaso.PROPOSTA_ENVIADA, AcaoCaso.ASSUMIR_ANALISE))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void estadoTerminalNaoAceitaAcoes() {
        assertThatThrownBy(() -> MaquinaEstadosCaso.proximoEstado(
                CasoTipo.RMG, EstadoCaso.APROVADO, AcaoCaso.CANCELAR))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void cancelarEhPermitidoEmEstadoNaoTerminal() {
        assertThat(MaquinaEstadosCaso.proximoEstado(CasoTipo.RMG, EstadoCaso.EM_ANALISE, AcaoCaso.CANCELAR))
                .isEqualTo(EstadoCaso.CANCELADO);
    }

    @Test
    void linhasEditaveisApenasAntesDaAprovacao() {
        assertThat(MaquinaEstadosCaso.podeEditarLinhas(EstadoCaso.EM_ANALISE)).isTrue();
        assertThat(MaquinaEstadosCaso.podeEditarLinhas(EstadoCaso.PROPOSTA_ENVIADA)).isFalse();
        assertThat(MaquinaEstadosCaso.podeEditarLinhas(EstadoCaso.APROVADO)).isFalse();
    }
}
