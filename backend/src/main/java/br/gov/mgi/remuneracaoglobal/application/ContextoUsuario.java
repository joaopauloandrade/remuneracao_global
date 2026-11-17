package br.gov.mgi.remuneracaoglobal.application;

import java.util.Set;

/**
 * Contexto do usuário autenticado. Construído a partir do token pela camada de infraestrutura.
 */
public record ContextoUsuario(String usuario, Set<Perfil> perfis, String empresa) {

    public boolean admin() {
        return perfis.contains(Perfil.ADMIN);
    }

    public boolean gestor() {
        return perfis.contains(Perfil.GESTOR);
    }

    public boolean tecnico() {
        return perfis.contains(Perfil.TECNICO);
    }

    public boolean preenchedor() {
        return perfis.contains(Perfil.PREENCHEDOR);
    }

    /** Preenchedores enxergam apenas a própria empresa; os demais perfis enxergam todas. */
    public boolean podeAcessarEmpresa(String empresaCodigo) {
        if (admin() || gestor() || tecnico()) {
            return true;
        }
        return preenchedor() && empresa != null && empresa.equals(empresaCodigo);
    }
}
