package br.gov.mgi.remuneracaoglobal.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "instancia_gestao")
public class InstanciaGestaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "data_desativacao")
    private LocalDateTime dataDesativacao;

    @Column(name = "motivo_desativacao", length = 500)
    private String motivoDesativacao;

    protected InstanciaGestaoEntity() {
    }

    public InstanciaGestaoEntity(String codigo, String nome, String descricao) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
    }

    public void atualizar(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public void desativar(String motivo, LocalDateTime agora) {
        this.ativo = false;
        this.motivoDesativacao = motivo;
        this.dataDesativacao = agora;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getDataDesativacao() {
        return dataDesativacao;
    }

    public String getMotivoDesativacao() {
        return motivoDesativacao;
    }
}
