package br.gov.mgi.remuneracaoglobal.infrastructure.security;

import br.gov.mgi.remuneracaoglobal.application.ContextoUsuario;
import br.gov.mgi.remuneracaoglobal.application.Perfil;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Converte as claims do token OIDC no contexto de usuário da aplicação.
 * Grupos do IdP são mapeados para perfis; a claim {@code company_id} define a empresa.
 */
public final class ContextoUsuarioJwt {

    private static final Map<String, Perfil> GRUPOS = Map.of(
            "RMG Preenchedor", Perfil.PREENCHEDOR,
            "RMG_Portal_Submitters", Perfil.PREENCHEDOR,
            "RMG Área Técnica", Perfil.TECNICO,
            "RMG_Technical_Staff", Perfil.TECNICO,
            "RMG Gestor", Perfil.GESTOR,
            "RMG_Managers", Perfil.GESTOR,
            "RMG Administrador", Perfil.ADMIN,
            "RMG_Admins", Perfil.ADMIN);

    private ContextoUsuarioJwt() {
    }

    public static ContextoUsuario de(Jwt jwt) {
        String usuario = jwt.getClaimAsString("preferred_username");
        if (usuario == null) {
            usuario = jwt.getSubject();
        }
        return new ContextoUsuario(usuario, perfis(jwt.getClaim("groups")), jwt.getClaimAsString("company_id"));
    }

    private static Set<Perfil> perfis(Collection<?> grupos) {
        Set<Perfil> perfis = EnumSet.noneOf(Perfil.class);
        if (grupos != null) {
            for (Object grupo : grupos) {
                Perfil perfil = GRUPOS.get(String.valueOf(grupo));
                if (perfil != null) {
                    perfis.add(perfil);
                }
            }
        }
        return perfis;
    }
}
