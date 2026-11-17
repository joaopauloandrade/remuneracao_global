package br.gov.mgi.remuneracaoglobal.application;

import br.gov.mgi.remuneracaoglobal.domain.AcaoCaso;
import br.gov.mgi.remuneracaoglobal.domain.CasoTipo;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.CasoEntity;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.LinhaPropostaEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class Modelos {

    private Modelos() {
    }

    /** Linha com os nomes de órgão e cargo resolvidos. */
    public record LinhaDetalhe(LinhaPropostaEntity linha, String instancia, String cargo) {
    }

    public record Totais(BigDecimal vigente, BigDecimal proposto, BigDecimal variacao) {
    }

    public record CriarCaso(CasoTipo tipo, String empresaCodigo, int exercicio,
                            String tipoAssembleia, LocalDate dataAssembleia) {
    }

    public record Cabecalho(String tipoAssembleia, LocalDate dataAssembleia) {
    }

    public record Linha(Long instanciaId, Long cargoId, int quantidadeMembros,
                        BigDecimal honorarioProposto, BigDecimal beneficioProposto) {
    }

    public record Acao(AcaoCaso acao, String justificativa, String motivo) {
    }

    public record Detalhe(CasoEntity caso,
                          List<LinhaDetalhe> linhas, Totais totais) {
    }
}
