package br.gov.mgi.remuneracaoglobal.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
public class AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(nullable = false, length = 50)
    private String acao;

    @Column(nullable = false, length = 50)
    private String entidade;

    @Column(name = "entidade_id", nullable = false, length = 50)
    private String entidadeId;

    @Column(length = 1000)
    private String detalhe;

    @Column(name = "registrado_em", nullable = false)
    private LocalDateTime registradoEm;

    protected AuditoriaEntity() {
    }

    public AuditoriaEntity(String usuario, String acao, String entidade, String entidadeId,
                           String detalhe, LocalDateTime registradoEm) {
        this.usuario = usuario;
        this.acao = acao;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.detalhe = detalhe;
        this.registradoEm = registradoEm;
    }

    public Long getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getAcao() {
        return acao;
    }

    public String getEntidade() {
        return entidade;
    }

    public String getEntidadeId() {
        return entidadeId;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public LocalDateTime getRegistradoEm() {
        return registradoEm;
    }
}
