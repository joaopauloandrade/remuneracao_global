package br.gov.mgi.remuneracaoglobal.infrastructure.web;

import br.gov.mgi.remuneracaoglobal.application.CasoService;
import br.gov.mgi.remuneracaoglobal.application.Modelos;
import br.gov.mgi.remuneracaoglobal.infrastructure.export.ExportadorExcel;
import br.gov.mgi.remuneracaoglobal.infrastructure.export.NotaTecnicaPdf;
import br.gov.mgi.remuneracaoglobal.infrastructure.security.ContextoUsuarioJwt;
import br.gov.mgi.remuneracaoglobal.infrastructure.web.ApiDtos.AcaoRequest;
import br.gov.mgi.remuneracaoglobal.infrastructure.web.ApiDtos.CabecalhoRequest;
import br.gov.mgi.remuneracaoglobal.infrastructure.web.ApiDtos.CasoDetalhe;
import br.gov.mgi.remuneracaoglobal.infrastructure.web.ApiDtos.CasoResumo;
import br.gov.mgi.remuneracaoglobal.infrastructure.web.ApiDtos.CriarCasoRequest;
import br.gov.mgi.remuneracaoglobal.infrastructure.web.ApiDtos.LinhaRequest;
import br.gov.mgi.remuneracaoglobal.infrastructure.web.ApiDtos.LinhaResposta;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/casos")
public class CasoController {

    private final CasoService casos;
    private final ExportadorExcel exportadorExcel;
    private final NotaTecnicaPdf notaTecnicaPdf;

    public CasoController(CasoService casos, ExportadorExcel exportadorExcel, NotaTecnicaPdf notaTecnicaPdf) {
        this.casos = casos;
        this.exportadorExcel = exportadorExcel;
        this.notaTecnicaPdf = notaTecnicaPdf;
    }

    @GetMapping
    public List<CasoResumo> listar(@AuthenticationPrincipal Jwt jwt,
                                   @RequestParam(defaultValue = "false") boolean minhas) {
        return casos.listar(ContextoUsuarioJwt.de(jwt), minhas).stream().map(CasoResumo::de).toList();
    }

    @PostMapping
    public ResponseEntity<CasoResumo> criar(@AuthenticationPrincipal Jwt jwt,
                                            @Valid @RequestBody CriarCasoRequest req) {
        var cmd = new Modelos.CriarCaso(req.tipo(), req.empresaCodigo(), req.exercicio(),
                req.tipoAssembleia(), req.dataAssembleia());
        var caso = casos.criar(ContextoUsuarioJwt.de(jwt), cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(CasoResumo.de(caso));
    }

    @GetMapping("/{id}")
    public CasoDetalhe detalhe(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return CasoDetalhe.de(casos.detalhe(ContextoUsuarioJwt.de(jwt), id));
    }

    @PutMapping("/{id}/cabecalho")
    public CasoResumo atualizarCabecalho(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                         @Valid @RequestBody CabecalhoRequest req) {
        var cmd = new Modelos.Cabecalho(req.tipoAssembleia(), req.dataAssembleia());
        return CasoResumo.de(casos.atualizarCabecalho(ContextoUsuarioJwt.de(jwt), id, cmd));
    }

    @PostMapping("/{id}/linhas")
    public ResponseEntity<LinhaResposta> adicionarLinha(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                        @Valid @RequestBody LinhaRequest req) {
        var linha = casos.adicionarLinha(ContextoUsuarioJwt.de(jwt), id, paraLinha(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(LinhaResposta.de(linha));
    }

    @PutMapping("/{id}/linhas/{linhaId}")
    public LinhaResposta atualizarLinha(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                        @PathVariable Long linhaId, @Valid @RequestBody LinhaRequest req) {
        return LinhaResposta.de(casos.atualizarLinha(ContextoUsuarioJwt.de(jwt), id, linhaId, paraLinha(req)));
    }

    @DeleteMapping("/{id}/linhas/{linhaId}")
    public ResponseEntity<Void> excluirLinha(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                             @PathVariable Long linhaId) {
        casos.excluirLinha(ContextoUsuarioJwt.de(jwt), id, linhaId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/acoes")
    public CasoResumo executarAcao(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                   @Valid @RequestBody AcaoRequest req) {
        var cmd = new Modelos.Acao(req.acao(), req.justificativa(), req.motivo());
        return CasoResumo.de(casos.executarAcao(ContextoUsuarioJwt.de(jwt), id, cmd));
    }

    @GetMapping("/{id}/exportacao/excel")
    public ResponseEntity<byte[]> exportarExcel(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        var detalhe = casos.detalhe(ContextoUsuarioJwt.de(jwt), id);
        return arquivo(exportadorExcel.gerar(detalhe), detalhe.caso().getNumero() + ".xlsx",
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @GetMapping("/{id}/exportacao/pdf")
    public ResponseEntity<byte[]> exportarPdf(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        var detalhe = casos.detalhe(ContextoUsuarioJwt.de(jwt), id);
        return arquivo(notaTecnicaPdf.gerar(detalhe), detalhe.caso().getNumero() + ".pdf",
                MediaType.APPLICATION_PDF);
    }

    private static Modelos.Linha paraLinha(LinhaRequest req) {
        return new Modelos.Linha(req.instanciaId(), req.cargoId(), req.quantidadeMembros(),
                req.honorarioProposto(), req.beneficioProposto());
    }

    private static ResponseEntity<byte[]> arquivo(byte[] conteudo, String nome, MediaType tipo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome + "\"")
                .contentType(tipo)
                .body(conteudo);
    }
}
