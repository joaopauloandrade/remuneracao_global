package br.gov.mgi.remuneracaoglobal.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_remuneracao")
public class HistoricoRemuneracaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_codigo", nullable = false, length = 20)
    private String empresaCodigo;

    @Column(nullable = false)
    private int exercicio;

    @Column(name = "instancia_id", nullable = false)
    private Long instanciaId;

    @Column(name = "cargo_id", nullable = false)
    private Long cargoId;

    @Column(name = "honorario_aprovado", nullable = false, precision = 18, scale = 2)
    private BigDecimal honorarioAprovado;

    @Column(name = "beneficio_aprovado", nullable = false, precision = 18, scale = 2)
    private BigDecimal beneficioAprovado;

    @Column(name = "quantidade_membros", nullable = false)
    private int quantidadeMembros;

    @Column(name = "numero_caso", nullable = false, length = 20)
    private String numeroCaso;

    @Column(length = 100)
    private String portaria;

    @Column(name = "data_dou")
    private LocalDate dataDou;

    @Column(name = "registrado_em", nullable = false)
    private LocalDateTime registradoEm;

    protected HistoricoRemuneracaoEntity() {
    }

    public HistoricoRemuneracaoEntity(String empresaCodigo, int exercicio, Long instanciaId, Long cargoId,
                                      BigDecimal honorarioAprovado, BigDecimal beneficioAprovado,
                                      int quantidadeMembros, String numeroCaso, String portaria,
                                      LocalDate dataDou, LocalDateTime registradoEm) {
        this.empresaCodigo = empresaCodigo;
        this.exercicio = exercicio;
        this.instanciaId = instanciaId;
        this.cargoId = cargoId;
        this.honorarioAprovado = honorarioAprovado;
        this.beneficioAprovado = beneficioAprovado;
        this.quantidadeMembros = quantidadeMembros;
        this.numeroCaso = numeroCaso;
        this.portaria = portaria;
        this.dataDou = dataDou;
        this.registradoEm = registradoEm;
    }

    public String getEmpresaCodigo() {
        return empresaCodigo;
    }

    public int getExercicio() {
        return exercicio;
    }

    public Long getInstanciaId() {
        return instanciaId;
    }

    public Long getCargoId() {
        return cargoId;
    }

    public BigDecimal getHonorarioAprovado() {
        return honorarioAprovado;
    }

    public BigDecimal getBeneficioAprovado() {
        return beneficioAprovado;
    }

    public int getQuantidadeMembros() {
        return quantidadeMembros;
    }

    public String getNumeroCaso() {
        return numeroCaso;
    }
}
