package br.gov.mgi.remuneracaoglobal.infrastructure.web;

import br.gov.mgi.remuneracaoglobal.application.Modelos;
import br.gov.mgi.remuneracaoglobal.domain.AcaoCaso;
import br.gov.mgi.remuneracaoglobal.domain.CasoTipo;
import br.gov.mgi.remuneracaoglobal.domain.EstadoCaso;
import br.gov.mgi.remuneracaoglobal.domain.EstadoLinha;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.CargoEntity;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.CasoEntity;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.EmpresaEntity;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.InstanciaGestaoEntity;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.LinhaPropostaEntity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Contratos HTTP da API. Requisições validadas com Jakarta Validation; respostas nunca expõem entidades. */
public final class ApiDtos {

    private ApiDtos() {
    }

    // ---- Requisições ----

    public record CriarCasoRequest(
            @NotNull CasoTipo tipo,
            @NotBlank @Size(max = 20) String empresaCodigo,
            @Min(2000) @Max(2100) int exercicio,
            @NotBlank @Size(max = 10) String tipoAssembleia,
            LocalDate dataAssembleia) {
    }

    public record CabecalhoRequest(
            @NotBlank @Size(max = 10) String tipoAssembleia,
            LocalDate dataAssembleia) {
    }

    public record LinhaRequest(
            @NotNull Long instanciaId,
            @NotNull Long cargoId,
            @Min(1) int quantidadeMembros,
            @NotNull @PositiveOrZero BigDecimal honorarioProposto,
            @NotNull @PositiveOrZero BigDecimal beneficioProposto) {
    }

    public record AcaoRequest(
            @NotNull AcaoCaso acao,
            @Size(max = 4000) String justificativa,
            @Size(max = 4000) String motivo) {
    }

    public record InstanciaRequest(
            @NotBlank @Size(max = 20) String codigo,
            @NotBlank @Size(max = 200) String nome,
            @Size(max = 1000) String descricao) {
    }

    public record AtualizarInstanciaRequest(
            @NotBlank @Size(max = 200) String nome,
            @Size(max = 1000) String descricao) {
    }

    public record CargoRequest(
            @NotBlank @Size(max = 20) String codigo,
            @NotBlank @Size(max = 200) String nome) {
    }

    public record AtualizarCargoRequest(
            @NotBlank @Size(max = 200) String nome) {
    }

    public record MotivoRequest(
            @NotBlank @Size(max = 1000) String motivo) {
    }

    // ---- Respostas ----

    public record CasoResumo(
            Long id,
            String numero,
            CasoTipo tipo,
            String empresaCodigo,
            int exercicio,
            String tipoAssembleia,
            LocalDate dataAssembleia,
            EstadoCaso estado,
            String justificativa,
            String responsavel,
            String criadoPor,
            LocalDateTime criadoEm,
            LocalDateTime atualizadoEm) {

        public static CasoResumo de(CasoEntity c) {
            return new CasoResumo(c.getId(), c.getNumero(), c.getTipo(), c.getEmpresaCodigo(), c.getExercicio(),
                    c.getTipoAssembleia(), c.getDataAssembleia(), c.getEstado(), c.getJustificativa(),
                    c.getResponsavel(), c.getCriadoPor(), c.getCriadoEm(), c.getAtualizadoEm());
        }
    }

    public record TotaisResposta(BigDecimal vigente, BigDecimal proposto, BigDecimal variacaoPercentual) {

        static TotaisResposta de(Modelos.Totais t) {
            return new TotaisResposta(t.vigente(), t.proposto(), t.variacao());
        }
    }

    public record CasoDetalhe(CasoResumo caso, List<LinhaResposta> linhas, TotaisResposta totais) {

        public static CasoDetalhe de(Modelos.Detalhe d) {
            return new CasoDetalhe(CasoResumo.de(d.caso()),
                    d.linhas().stream().map(LinhaResposta::de).toList(),
                    TotaisResposta.de(d.totais()));
        }
    }

    public record LinhaResposta(
            Long id,
            Long instanciaId,
            String instancia,
            Long cargoId,
            String cargo,
            int quantidadeMembros,
            BigDecimal honorarioVigente,
            BigDecimal honorarioProposto,
            BigDecimal beneficioVigente,
            BigDecimal beneficioProposto,
            BigDecimal totalVigente,
            BigDecimal totalProposto,
            BigDecimal variacaoPercentual,
            EstadoLinha estado) {

        public static LinhaResposta de(Modelos.LinhaDetalhe ld) {
            LinhaPropostaEntity l = ld.linha();
            return new LinhaResposta(l.getId(), l.getInstanciaId(), ld.instancia(), l.getCargoId(), ld.cargo(),
                    l.getQuantidadeMembros(), l.getHonorarioVigente(), l.getHonorarioProposto(),
                    l.getBeneficioVigente(), l.getBeneficioProposto(), l.getTotalVigente(), l.getTotalProposto(),
                    l.getVariacaoPercentual(), l.getEstado());
        }
    }

    public record InstanciaResposta(Long id, String codigo, String nome, String descricao, boolean ativo) {

        public static InstanciaResposta de(InstanciaGestaoEntity i) {
            return new InstanciaResposta(i.getId(), i.getCodigo(), i.getNome(), i.getDescricao(), i.isAtivo());
        }
    }

    public record CargoResposta(Long id, String codigo, String nome, boolean ativo) {

        public static CargoResposta de(CargoEntity c) {
            return new CargoResposta(c.getId(), c.getCodigo(), c.getNome(), c.isAtivo());
        }
    }

    public record EmpresaResposta(String codigo, String sigla, String setor, String porte, String controle,
                                  boolean controladora) {

        public static EmpresaResposta de(EmpresaEntity e) {
            return new EmpresaResposta(e.getCodigo(), e.getSigla(), e.getSetor(), e.getPorte(), e.getControle(),
                    e.isControladora());
        }
    }
}
