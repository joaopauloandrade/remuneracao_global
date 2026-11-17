package br.gov.mgi.remuneracaoglobal.infrastructure.persistence;

import br.gov.mgi.remuneracaoglobal.domain.CasoTipo;
import br.gov.mgi.remuneracaoglobal.domain.EstadoCaso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "caso")
public class CasoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CasoTipo tipo;

    @Column(name = "empresa_codigo", nullable = false, length = 20)
    private String empresaCodigo;

    @Column(nullable = false)
    private int exercicio;

    @Column(name = "tipo_assembleia", nullable = false, length = 10)
    private String tipoAssembleia;

    @Column(name = "data_assembleia")
    private LocalDate dataAssembleia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoCaso estado;

    @Column(length = 4000)
    private String justificativa;

    @Column(length = 100)
    private String responsavel;

    @Column(name = "criado_por", nullable = false, length = 100)
    private String criadoPor;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    protected CasoEntity() {
    }

    public CasoEntity(CasoTipo tipo, String empresaCodigo, int exercicio, String tipoAssembleia,
                      LocalDate dataAssembleia, String criadoPor, LocalDateTime agora) {
        this.tipo = tipo;
        this.empresaCodigo = empresaCodigo;
        this.exercicio = exercicio;
        this.tipoAssembleia = tipoAssembleia;
        this.dataAssembleia = dataAssembleia;
        this.estado = EstadoCaso.RASCUNHO;
        this.criadoPor = criadoPor;
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    /** Numeração: prefixo do tipo + id com 7 dígitos (ex.: RMG0001001). */
    public void atribuirNumero() {
        this.numero = tipo.prefixo() + String.format("%07d", id);
    }

    public void atualizarCabecalho(String tipoAssembleia, LocalDate dataAssembleia, LocalDateTime agora) {
        this.tipoAssembleia = tipoAssembleia;
        this.dataAssembleia = dataAssembleia;
        this.atualizadoEm = agora;
    }

    public void alterarEstado(EstadoCaso novoEstado, String responsavel, LocalDateTime agora) {
        this.estado = novoEstado;
        if (responsavel != null) {
            this.responsavel = responsavel;
        }
        this.atualizadoEm = agora;
    }

    public void registrarJustificativa(String texto, LocalDateTime agora) {
        this.justificativa = texto;
        this.atualizadoEm = agora;
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public CasoTipo getTipo() {
        return tipo;
    }

    public String getEmpresaCodigo() {
        return empresaCodigo;
    }

    public int getExercicio() {
        return exercicio;
    }

    public String getTipoAssembleia() {
        return tipoAssembleia;
    }

    public LocalDate getDataAssembleia() {
        return dataAssembleia;
    }

    public EstadoCaso getEstado() {
        return estado;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public String getCriadoPor() {
        return criadoPor;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
