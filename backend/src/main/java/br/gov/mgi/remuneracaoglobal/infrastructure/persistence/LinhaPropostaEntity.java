package br.gov.mgi.remuneracaoglobal.infrastructure.persistence;

import br.gov.mgi.remuneracaoglobal.domain.EstadoLinha;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "linha_proposta")
public class LinhaPropostaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caso_id", nullable = false)
    private Long casoId;

    @Column(name = "instancia_id", nullable = false)
    private Long instanciaId;

    @Column(name = "cargo_id", nullable = false)
    private Long cargoId;

    @Column(name = "quantidade_membros", nullable = false)
    private int quantidadeMembros;

    @Column(name = "honorario_vigente", nullable = false, precision = 18, scale = 2)
    private BigDecimal honorarioVigente;

    @Column(name = "honorario_proposto", nullable = false, precision = 18, scale = 2)
    private BigDecimal honorarioProposto;

    @Column(name = "beneficio_vigente", nullable = false, precision = 18, scale = 2)
    private BigDecimal beneficioVigente;

    @Column(name = "beneficio_proposto", nullable = false, precision = 18, scale = 2)
    private BigDecimal beneficioProposto;

    @Column(name = "total_vigente", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalVigente;

    @Column(name = "total_proposto", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalProposto;

    @Column(name = "variacao_percentual", nullable = false, precision = 10, scale = 2)
    private BigDecimal variacaoPercentual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoLinha estado;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    protected LinhaPropostaEntity() {
    }

    public LinhaPropostaEntity(Long casoId, Long instanciaId, Long cargoId, LocalDateTime agora) {
        this.casoId = casoId;
        this.instanciaId = instanciaId;
        this.cargoId = cargoId;
        this.estado = EstadoLinha.PROPOSTA;
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    public void atualizarValores(Long instanciaId, Long cargoId, int membros,
                                 BigDecimal honorarioVigente, BigDecimal honorarioProposto,
                                 BigDecimal beneficioVigente, BigDecimal beneficioProposto,
                                 BigDecimal totalVigente, BigDecimal totalProposto,
                                 BigDecimal variacao, LocalDateTime agora) {
        this.instanciaId = instanciaId;
        this.cargoId = cargoId;
        this.quantidadeMembros = membros;
        this.honorarioVigente = honorarioVigente;
        this.honorarioProposto = honorarioProposto;
        this.beneficioVigente = beneficioVigente;
        this.beneficioProposto = beneficioProposto;
        this.totalVigente = totalVigente;
        this.totalProposto = totalProposto;
        this.variacaoPercentual = variacao;
        this.atualizadoEm = agora;
        if (this.estado == EstadoLinha.PROPOSTA) {
            this.estado = EstadoLinha.EDITADA;
        }
    }

    public void excluir(LocalDateTime agora) {
        this.estado = EstadoLinha.EXCLUIDA;
        this.atualizadoEm = agora;
    }

    public Long getId() {
        return id;
    }

    public Long getCasoId() {
        return casoId;
    }

    public Long getInstanciaId() {
        return instanciaId;
    }

    public Long getCargoId() {
        return cargoId;
    }

    public int getQuantidadeMembros() {
        return quantidadeMembros;
    }

    public BigDecimal getHonorarioVigente() {
        return honorarioVigente;
    }

    public BigDecimal getHonorarioProposto() {
        return honorarioProposto;
    }

    public BigDecimal getBeneficioVigente() {
        return beneficioVigente;
    }

    public BigDecimal getBeneficioProposto() {
        return beneficioProposto;
    }

    public BigDecimal getTotalVigente() {
        return totalVigente;
    }

    public BigDecimal getTotalProposto() {
        return totalProposto;
    }

    public BigDecimal getVariacaoPercentual() {
        return variacaoPercentual;
    }

    public EstadoLinha getEstado() {
        return estado;
    }
}
