package br.gov.mgi.remuneracaoglobal.infrastructure.web;

import br.gov.mgi.remuneracaoglobal.application.CasoService;
import br.gov.mgi.remuneracaoglobal.domain.EstadoCaso;
import br.gov.mgi.remuneracaoglobal.infrastructure.security.ContextoUsuarioJwt;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CasoService casos;

    public DashboardController(CasoService casos) {
        this.casos = casos;
    }

    @GetMapping("/resumo")
    public Map<EstadoCaso, Long> resumo(@AuthenticationPrincipal Jwt jwt) {
        return casos.resumoPorEstado(ContextoUsuarioJwt.de(jwt));
    }
}
