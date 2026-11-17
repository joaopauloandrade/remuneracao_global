package br.gov.mgi.remuneracaoglobal.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "proposta_enviada")
public class PropostaEnviadaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caso_id", nullable = false)
    private Long casoId;

    @Column(nullable = false)
    private int versao;

    @Column(name = "enviado_por", nullable = false, length = 100)
    private String enviadoPor;

    @Column(name = "enviado_em", nullable = false)
    private LocalDateTime enviadoEm;

    @Column(name = "total_vigente", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalVigente;

    @Column(name = "total_proposto", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalProposto;

    @Column(name = "variacao_percentual", nullable = false, precision = 10, scale = 2)
    private BigDecimal variacaoPercentual;

    @Column(nullable = false, length = 20000)
    private String snapshot;

    protected PropostaEnviadaEntity() {
    }

    public PropostaEnviadaEntity(Long casoId, int versao, String enviadoPor, LocalDateTime enviadoEm,
                                 BigDecimal totalVigente, BigDecimal totalProposto,
                                 BigDecimal variacaoPercentual, String snapshot) {
        this.casoId = casoId;
        this.versao = versao;
        this.enviadoPor = enviadoPor;
        this.enviadoEm = enviadoEm;
        this.totalVigente = totalVigente;
        this.totalProposto = totalProposto;
        this.variacaoPercentual = variacaoPercentual;
        this.snapshot = snapshot;
    }

    public Long getId() {
        return id;
    }

    public Long getCasoId() {
        return casoId;
    }

    public int getVersao() {
        return versao;
    }

    public String getEnviadoPor() {
        return enviadoPor;
    }

    public LocalDateTime getEnviadoEm() {
        return enviadoEm;
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

    public String getSnapshot() {
        return snapshot;
    }
}
