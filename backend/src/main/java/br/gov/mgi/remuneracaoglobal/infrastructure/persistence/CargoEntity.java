package br.gov.mgi.remuneracaoglobal.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "cargo")
public class CargoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "data_desativacao")
    private LocalDateTime dataDesativacao;

    @Column(name = "motivo_desativacao", length = 500)
    private String motivoDesativacao;

    protected CargoEntity() {
    }

    public CargoEntity(String codigo, String nome) {
        this.codigo = codigo;
        this.nome = nome;
    }

    public void atualizar(String nome) {
        this.nome = nome;
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
