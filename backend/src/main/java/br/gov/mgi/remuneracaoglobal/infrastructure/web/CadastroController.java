package br.gov.mgi.remuneracaoglobal.infrastructure.web;

import br.gov.mgi.remuneracaoglobal.application.CadastroService;
import br.gov.mgi.remuneracaoglobal.infrastructure.security.ContextoUsuarioJwt;
import br.gov.mgi.remuneracaoglobal.infrastructure.web.ApiDtos.AtualizarCargoRequest;
import br.gov.mgi.remuneracaoglobal.infrastructure.web.ApiDtos.AtualizarInstanciaRequest;
import br.gov.mgi.remuneracaoglobal.infrastructure.web.ApiDtos.CargoRequest;
import br.gov.mgi.remuneracaoglobal.infrastructure.web.ApiDtos.CargoResposta;
import br.gov.mgi.remuneracaoglobal.infrastructure.web.ApiDtos.EmpresaResposta;
import br.gov.mgi.remuneracaoglobal.infrastructure.web.ApiDtos.InstanciaRequest;
import br.gov.mgi.remuneracaoglobal.infrastructure.web.ApiDtos.InstanciaResposta;
import br.gov.mgi.remuneracaoglobal.infrastructure.web.ApiDtos.MotivoRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Cadastros de referência. Gravação de instâncias e cargos é restrita a ADMIN (validada no serviço). */
@RestController
@RequestMapping("/api/cadastros")
public class CadastroController {

    private final CadastroService cadastros;

    public CadastroController(CadastroService cadastros) {
        this.cadastros = cadastros;
    }

    @GetMapping("/empresas")
    public List<EmpresaResposta> empresas() {
        return cadastros.listarEmpresas().stream().map(EmpresaResposta::de).toList();
    }

    @GetMapping("/instancias")
    public List<InstanciaResposta> instancias() {
        return cadastros.listarInstancias().stream().map(InstanciaResposta::de).toList();
    }

    @PostMapping("/instancias")
    public ResponseEntity<InstanciaResposta> criarInstancia(@AuthenticationPrincipal Jwt jwt,
                                                            @Valid @RequestBody InstanciaRequest req) {
        var i = cadastros.criarInstancia(ContextoUsuarioJwt.de(jwt), req.codigo(), req.nome(), req.descricao());
        return ResponseEntity.status(HttpStatus.CREATED).body(InstanciaResposta.de(i));
    }

    @PutMapping("/instancias/{id}")
    public InstanciaResposta atualizarInstancia(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                @Valid @RequestBody AtualizarInstanciaRequest req) {
        var i = cadastros.atualizarInstancia(ContextoUsuarioJwt.de(jwt), id, req.nome(), req.descricao());
        return InstanciaResposta.de(i);
    }

    @PostMapping("/instancias/{id}/desativacao")
    public ResponseEntity<Void> desativarInstancia(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                   @Valid @RequestBody MotivoRequest req) {
        cadastros.desativarInstancia(ContextoUsuarioJwt.de(jwt), id, req.motivo());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cargos")
    public List<CargoResposta> cargos() {
        return cadastros.listarCargos().stream().map(CargoResposta::de).toList();
    }

    @PostMapping("/cargos")
    public ResponseEntity<CargoResposta> criarCargo(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody CargoRequest req) {
        var c = cadastros.criarCargo(ContextoUsuarioJwt.de(jwt), req.codigo(), req.nome());
        return ResponseEntity.status(HttpStatus.CREATED).body(CargoResposta.de(c));
    }

    @PutMapping("/cargos/{id}")
    public CargoResposta atualizarCargo(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                        @Valid @RequestBody AtualizarCargoRequest req) {
        return CargoResposta.de(cadastros.atualizarCargo(ContextoUsuarioJwt.de(jwt), id, req.nome()));
    }

    @PostMapping("/cargos/{id}/desativacao")
    public ResponseEntity<Void> desativarCargo(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                               @Valid @RequestBody MotivoRequest req) {
        cadastros.desativarCargo(ContextoUsuarioJwt.de(jwt), id, req.motivo());
        return ResponseEntity.noContent().build();
    }
}
