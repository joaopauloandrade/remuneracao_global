package br.gov.mgi.remuneracaoglobal.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "empresa")
public class EmpresaEntity {

    @Id
    @Column(length = 20)
    private String codigo;

    @Column(nullable = false, length = 50)
    private String sigla;

    @Column(length = 100)
    private String setor;

    @Column(length = 50)
    private String porte;

    @Column(length = 50)
    private String controle;

    @Column(nullable = false)
    private boolean controladora;

    protected EmpresaEntity() {
    }

    public String getCodigo() {
        return codigo;
    }

    public String getSigla() {
        return sigla;
    }

    public String getSetor() {
        return setor;
    }

    public String getPorte() {
        return porte;
    }

    public String getControle() {
        return controle;
    }

    public boolean isControladora() {
        return controladora;
    }
}
