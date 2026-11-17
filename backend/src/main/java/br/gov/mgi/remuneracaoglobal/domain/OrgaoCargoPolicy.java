package br.gov.mgi.remuneracaoglobal.domain;

import java.util.Map;
import java.util.Set;

/**
 * Matriz Instância × Cargo (SPEC §5.2). Instâncias sem regra cadastrada não são restringidas.
 */
public final class OrgaoCargoPolicy {

    private static final Map<String, Set<String>> MATRIZ = Map.of(
            "Diretoria", Set.of("Presidente", "Vice-Presidente", "Diretor", "Liquidante"),
            "Conselho de Administração", Set.of("Membro"),
            "Conselho Fiscal", Set.of("Membro"),
            "Comitê de Auditoria", Set.of("Presidente", "Membro"));

    private static final Set<String> CARGOS_DEMAIS_COMITES = Set.of("Presidente", "Membro");

    private OrgaoCargoPolicy() {
    }

    public static boolean permite(String instancia, String cargo) {
        Set<String> permitidos = MATRIZ.get(instancia);
        if (permitidos == null) {
            if (instancia.startsWith("Comitê")) {
                return CARGOS_DEMAIS_COMITES.contains(cargo);
            }
            return true;
        }
        return permitidos.contains(cargo);
    }
}
