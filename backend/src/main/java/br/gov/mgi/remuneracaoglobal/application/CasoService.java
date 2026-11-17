package br.gov.mgi.remuneracaoglobal.application;

import br.gov.mgi.remuneracaoglobal.application.Modelos.Acao;
import br.gov.mgi.remuneracaoglobal.application.Modelos.Cabecalho;
import br.gov.mgi.remuneracaoglobal.application.Modelos.CriarCaso;
import br.gov.mgi.remuneracaoglobal.application.Modelos.Detalhe;
import br.gov.mgi.remuneracaoglobal.application.Modelos.LinhaDetalhe;
import br.gov.mgi.remuneracaoglobal.application.Modelos.Linha;
import br.gov.mgi.remuneracaoglobal.application.Modelos.Totais;
import br.gov.mgi.remuneracaoglobal.domain.AcaoCaso;
import br.gov.mgi.remuneracaoglobal.domain.CalculadoraFinanceira;
import br.gov.mgi.remuneracaoglobal.domain.EstadoCaso;
import br.gov.mgi.remuneracaoglobal.domain.EstadoLinha;
import br.gov.mgi.remuneracaoglobal.domain.MaquinaEstadosCaso;
import br.gov.mgi.remuneracaoglobal.domain.OrgaoCargoPolicy;
import br.gov.mgi.remuneracaoglobal.domain.RecursoNaoEncontradoException;
import br.gov.mgi.remuneracaoglobal.domain.RegraNegocioException;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.CargoEntity;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.CargoRepository;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.CasoEntity;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.CasoRepository;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.EmpresaRepository;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.HistoricoRemuneracaoEntity;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.HistoricoRemuneracaoRepository;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.InstanciaGestaoEntity;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.InstanciaGestaoRepository;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.LinhaPropostaEntity;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.LinhaPropostaRepository;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.PropostaEnviadaEntity;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.PropostaEnviadaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CasoService {

    private static final String ENTIDADE = "CASO";
    private static final String ENTIDADE_LINHA = "LINHA_PROPOSTA";
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final CasoRepository casos;
    private final LinhaPropostaRepository linhas;
    private final PropostaEnviadaRepository propostas;
    private final HistoricoRemuneracaoRepository historico;
    private final InstanciaGestaoRepository instancias;
    private final CargoRepository cargos;
    private final EmpresaRepository empresas;
    private final AuditoriaService auditoria;
    private final ObjectMapper mapper;
    private final BigDecimal limiteVariacao;

    public CasoService(CasoRepository casos, LinhaPropostaRepository linhas, PropostaEnviadaRepository propostas,
                       HistoricoRemuneracaoRepository historico, InstanciaGestaoRepository instancias,
                       CargoRepository cargos, EmpresaRepository empresas, AuditoriaService auditoria,
                       ObjectMapper mapper,
                       @Value("${rmg.justificativa.limite-variacao-percentual:0}") BigDecimal limiteVariacao) {
        this.casos = casos;
        this.linhas = linhas;
        this.propostas = propostas;
        this.historico = historico;
        this.instancias = instancias;
        this.cargos = cargos;
        this.empresas = empresas;
        this.auditoria = auditoria;
        this.mapper = mapper;
        this.limiteVariacao = limiteVariacao;
    }

    @Transactional
    public CasoEntity criar(ContextoUsuario ctx, CriarCaso cmd) {
        boolean permitido = ctx.admin() || ctx.tecnico()
                || (ctx.preenchedor() && ctx.podeAcessarEmpresa(cmd.empresaCodigo()));
        if (!permitido) {
            throw new AcessoNegadoException("Perfil sem permissão para criar casos nesta empresa.");
        }
        if (!empresas.existsById(cmd.empresaCodigo())) {
            throw new RecursoNaoEncontradoException("Empresa " + cmd.empresaCodigo() + " não encontrada.");
        }
        LocalDateTime agora = LocalDateTime.now();
        CasoEntity caso = casos.save(new CasoEntity(cmd.tipo(), cmd.empresaCodigo(), cmd.exercicio(),
                cmd.tipoAssembleia(), cmd.dataAssembleia(), ctx.usuario(), agora));
        caso.atribuirNumero();
        auditoria.registrar(ctx.usuario(), "CRIAR", ENTIDADE, String.valueOf(caso.getId()),
                "Caso " + caso.getNumero() + " criado para " + caso.getEmpresaCodigo() + ".");
        return caso;
    }

    @Transactional(readOnly = true)
    public CasoEntity buscarVisivel(ContextoUsuario ctx, Long id) {
        CasoEntity caso = casos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Caso " + id + " não encontrado."));
        if (!ctx.podeAcessarEmpresa(caso.getEmpresaCodigo())) {
            throw new AcessoNegadoException("Caso fora do escopo da empresa do usuário.");
        }
        return caso;
    }

    @Transactional(readOnly = true)
    public List<CasoEntity> listar(ContextoUsuario ctx, boolean apenasMinhas) {
        return casos.findAllByOrderByAtualizadoEmDesc().stream()
                .filter(c -> ctx.podeAcessarEmpresa(c.getEmpresaCodigo()))
                .filter(c -> !apenasMinhas
                        || ctx.usuario().equals(c.getCriadoPor()) || ctx.usuario().equals(c.getResponsavel()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<EstadoCaso, Long> resumoPorEstado(ContextoUsuario ctx) {
        Map<EstadoCaso, Long> resumo = new EnumMap<>(EstadoCaso.class);
        for (EstadoCaso estado : EstadoCaso.values()) {
            resumo.put(estado, 0L);
        }
        listar(ctx, false).forEach(c -> resumo.merge(c.getEstado(), 1L, Long::sum));
        return resumo;
    }

    @Transactional(readOnly = true)
    public Detalhe detalhe(ContextoUsuario ctx, Long id) {
        CasoEntity caso = buscarVisivel(ctx, id);
        List<LinhaPropostaEntity> ativas = linhasAtivas(caso);
        List<LinhaDetalhe> itens = ativas.stream().map(this::detalharLinha).toList();
        return new Detalhe(caso, itens, totais(ativas));
    }

    @Transactional
    public CasoEntity atualizarCabecalho(ContextoUsuario ctx, Long casoId, Cabecalho cmd) {
        CasoEntity caso = buscarVisivel(ctx, casoId);
        exigirEdicao(ctx, caso);
        LocalDateTime agora = LocalDateTime.now();
        caso.atualizarCabecalho(cmd.tipoAssembleia(), cmd.dataAssembleia(), agora);
        auditoria.registrar(ctx.usuario(), "ATUALIZAR_CABECALHO", ENTIDADE, String.valueOf(casoId),
                "Cabeçalho atualizado.");
        return caso;
    }

    @Transactional
    public LinhaDetalhe adicionarLinha(ContextoUsuario ctx, Long casoId, Linha cmd) {
        CasoEntity caso = buscarVisivel(ctx, casoId);
        exigirEdicao(ctx, caso);
        validarReferencias(cmd);
        LocalDateTime agora = LocalDateTime.now();
        LinhaPropostaEntity linha = new LinhaPropostaEntity(casoId, cmd.instanciaId(), cmd.cargoId(), agora);
        aplicarValores(caso, linha, cmd, agora);
        linha = linhas.save(linha);
        auditoria.registrar(ctx.usuario(), "INCLUIR_LINHA", ENTIDADE_LINHA, String.valueOf(linha.getId()),
                "Linha incluída no caso " + casoId + ".");
        return detalharLinha(linha);
    }

    @Transactional
    public LinhaDetalhe atualizarLinha(ContextoUsuario ctx, Long casoId, Long linhaId, Linha cmd) {
        CasoEntity caso = buscarVisivel(ctx, casoId);
        exigirEdicao(ctx, caso);
        LinhaPropostaEntity linha = linhaAtiva(casoId, linhaId);
        validarReferencias(cmd);
        aplicarValores(caso, linha, cmd, LocalDateTime.now());
        auditoria.registrar(ctx.usuario(), "ATUALIZAR_LINHA", ENTIDADE_LINHA, String.valueOf(linhaId),
                "Linha atualizada no caso " + casoId + ".");
        return detalharLinha(linha);
    }

    @Transactional
    public void excluirLinha(ContextoUsuario ctx, Long casoId, Long linhaId) {
        CasoEntity caso = buscarVisivel(ctx, casoId);
        exigirEdicao(ctx, caso);
        LinhaPropostaEntity linha = linhaAtiva(casoId, linhaId);
        linha.excluir(LocalDateTime.now());
        auditoria.registrar(ctx.usuario(), "EXCLUIR_LINHA", ENTIDADE_LINHA, String.valueOf(linhaId),
                "Linha excluída do caso " + casoId + ".");
    }

    @Transactional
    public CasoEntity executarAcao(ContextoUsuario ctx, Long casoId, Acao cmd) {
        CasoEntity caso = buscarVisivel(ctx, casoId);
        AcaoCaso acao = cmd.acao();
        exigirPermissaoAcao(ctx, caso, acao);
        EstadoCaso anterior = caso.getEstado();
        EstadoCaso novo = MaquinaEstadosCaso.proximoEstado(caso.getTipo(), anterior, acao);
        LocalDateTime agora = LocalDateTime.now();
        String responsavel = null;

        switch (acao) {
            case ENVIAR -> enviar(ctx, caso, cmd.justificativa(), agora);
            case ASSUMIR_ANALISE -> responsavel = ctx.usuario();
            case DEVOLVER -> exigirMotivo(cmd.motivo());
            case APROVAR -> aprovar(caso, agora);
            case CANCELAR -> {
                // Sem efeitos colaterais além da mudança de estado.
            }
        }

        caso.alterarEstado(novo, responsavel, agora);
        String detalhe = "Estado " + anterior + " → " + novo
                + (cmd.motivo() != null && !cmd.motivo().isBlank() ? ". Motivo: " + cmd.motivo() : "");
        auditoria.registrar(ctx.usuario(), acao.name(), ENTIDADE, String.valueOf(casoId), detalhe);
        return caso;
    }

    private void exigirPermissaoAcao(ContextoUsuario ctx, CasoEntity caso, AcaoCaso acao) {
        boolean permitido = switch (acao) {
            case ENVIAR -> ctx.admin() || (ctx.preenchedor() && ehCriador(ctx, caso));
            case ASSUMIR_ANALISE -> ctx.admin() || ctx.tecnico();
            case DEVOLVER -> ctx.admin() || (ctx.tecnico() && ctx.usuario().equals(caso.getResponsavel()));
            case APROVAR -> ctx.admin() || ctx.gestor();
            case CANCELAR -> ctx.admin() || ctx.gestor() || ehCriador(ctx, caso);
        };
        if (!permitido) {
            throw new AcessoNegadoException("Perfil sem permissão para a ação " + acao + ".");
        }
    }

    private void enviar(ContextoUsuario ctx, CasoEntity caso, String justificativa, LocalDateTime agora) {
        List<LinhaPropostaEntity> ativas = linhasAtivas(caso);
        if (ativas.isEmpty()) {
            throw new RegraNegocioException("Inclua ao menos uma linha antes de enviar o caso.");
        }
        Totais totais = totais(ativas);
        boolean exigeJustificativa = totais.variacao().compareTo(limiteVariacao) > 0;
        boolean informada = justificativa != null && !justificativa.isBlank();
        if (exigeJustificativa && !informada) {
            throw new RegraNegocioException("Justificativa obrigatória: variação de " + totais.variacao()
                    + "% acima do limite de " + limiteVariacao + "%.");
        }
        if (informada) {
            caso.registrarJustificativa(justificativa, agora);
        }
        int versao = propostas.findFirstByCasoIdOrderByVersaoDesc(caso.getId())
                .map(p -> p.getVersao() + 1).orElse(1);
        propostas.save(new PropostaEnviadaEntity(caso.getId(), versao, ctx.usuario(), agora,
                totais.vigente(), totais.proposto(), totais.variacao(), snapshot(caso, ativas, totais)));
    }

    private void aprovar(CasoEntity caso, LocalDateTime agora) {
        for (LinhaPropostaEntity linha : linhasAtivas(caso)) {
            historico.save(new HistoricoRemuneracaoEntity(caso.getEmpresaCodigo(), caso.getExercicio(),
                    linha.getInstanciaId(), linha.getCargoId(), linha.getHonorarioProposto(),
                    linha.getBeneficioProposto(), linha.getQuantidadeMembros(), caso.getNumero(),
                    null, null, agora));
        }
    }

    private void exigirMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("Informe o motivo da devolução.");
        }
    }

    private void exigirEdicao(ContextoUsuario ctx, CasoEntity caso) {
        if (!MaquinaEstadosCaso.podeEditarLinhas(caso.getEstado())) {
            throw new RegraNegocioException("O caso no estado " + caso.getEstado() + " não permite edição.");
        }
        boolean preenchedorCriador = ctx.preenchedor() && ehCriador(ctx, caso)
                && caso.getEstado() != EstadoCaso.EM_ANALISE;
        boolean tecnicoResponsavel = ctx.tecnico() && caso.getEstado() == EstadoCaso.EM_ANALISE
                && ctx.usuario().equals(caso.getResponsavel());
        if (!(ctx.admin() || preenchedorCriador || tecnicoResponsavel)) {
            throw new AcessoNegadoException("Perfil sem permissão para editar este caso no estado atual.");
        }
    }

    private boolean ehCriador(ContextoUsuario ctx, CasoEntity caso) {
        return ctx.usuario().equals(caso.getCriadoPor());
    }

    private void validarReferencias(Linha cmd) {
        InstanciaGestaoEntity instancia = instancias.findById(cmd.instanciaId())
                .filter(InstanciaGestaoEntity::isAtivo)
                .orElseThrow(() -> new RegraNegocioException("Instância inexistente ou inativa."));
        CargoEntity cargo = cargos.findById(cmd.cargoId())
                .filter(CargoEntity::isAtivo)
                .orElseThrow(() -> new RegraNegocioException("Cargo inexistente ou inativo."));
        if (!OrgaoCargoPolicy.permite(instancia.getNome(), cargo.getNome())) {
            throw new RegraNegocioException("O cargo " + cargo.getNome() + " não é permitido na instância "
                    + instancia.getNome() + ".");
        }
        if (cmd.quantidadeMembros() < 1) {
            throw new RegraNegocioException("A quantidade de membros deve ser maior que zero.");
        }
        if (cmd.honorarioProposto().signum() < 0 || cmd.beneficioProposto().signum() < 0) {
            throw new RegraNegocioException("Honorário e benefício não podem ser negativos.");
        }
    }

    private void aplicarValores(CasoEntity caso, LinhaPropostaEntity linha, Linha cmd, LocalDateTime agora) {
        Vigentes vigentes = valoresVigentes(caso, cmd.instanciaId(), cmd.cargoId());
        int membros = cmd.quantidadeMembros();
        BigDecimal totalVigente = CalculadoraFinanceira.totalLinha(vigentes.honorario(), vigentes.beneficio(), membros);
        BigDecimal totalProposto = CalculadoraFinanceira.totalLinha(
                cmd.honorarioProposto(), cmd.beneficioProposto(), membros);
        BigDecimal variacao = CalculadoraFinanceira.variacaoPercentual(totalVigente, totalProposto);
        linha.atualizarValores(cmd.instanciaId(), cmd.cargoId(), membros,
                vigentes.honorario(), cmd.honorarioProposto(),
                vigentes.beneficio(), cmd.beneficioProposto(),
                totalVigente, totalProposto, variacao, agora);
    }

    /** Valores aprovados mais recentes anteriores ao exercício do caso; zero quando não há histórico. */
    private Vigentes valoresVigentes(CasoEntity caso, Long instanciaId, Long cargoId) {
        return historico
                .findFirstByEmpresaCodigoAndInstanciaIdAndCargoIdAndExercicioLessThanOrderByExercicioDescIdDesc(
                        caso.getEmpresaCodigo(), instanciaId, cargoId, caso.getExercicio())
                .map(h -> new Vigentes(h.getHonorarioAprovado(), h.getBeneficioAprovado()))
                .orElse(new Vigentes(ZERO, ZERO));
    }

    private LinhaPropostaEntity linhaAtiva(Long casoId, Long linhaId) {
        return linhas.findByIdAndCasoId(linhaId, casoId)
                .filter(l -> l.getEstado() != EstadoLinha.EXCLUIDA)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Linha " + linhaId + " não encontrada."));
    }

    private List<LinhaPropostaEntity> linhasAtivas(CasoEntity caso) {
        return linhas.findByCasoIdOrderByIdAsc(caso.getId()).stream()
                .filter(l -> l.getEstado() != EstadoLinha.EXCLUIDA)
                .toList();
    }

    private LinhaDetalhe detalharLinha(LinhaPropostaEntity linha) {
        String instancia = instancias.findById(linha.getInstanciaId())
                .map(InstanciaGestaoEntity::getNome).orElse("");
        String cargo = cargos.findById(linha.getCargoId()).map(CargoEntity::getNome).orElse("");
        return new LinhaDetalhe(linha, instancia, cargo);
    }

    private Totais totais(List<LinhaPropostaEntity> ativas) {
        BigDecimal vigente = ativas.stream().map(LinhaPropostaEntity::getTotalVigente)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal proposto = ativas.stream().map(LinhaPropostaEntity::getTotalProposto)
                .reduce(ZERO, BigDecimal::add);
        return new Totais(vigente, proposto, CalculadoraFinanceira.variacaoPercentual(vigente, proposto));
    }

    private String snapshot(CasoEntity caso, List<LinhaPropostaEntity> ativas, Totais totais) {
        List<SnapshotLinha> itens = ativas.stream().map(l -> {
            LinhaDetalhe d = detalharLinha(l);
            return new SnapshotLinha(d.instancia(), d.cargo(), l.getQuantidadeMembros(),
                    l.getHonorarioVigente(), l.getHonorarioProposto(),
                    l.getBeneficioVigente(), l.getBeneficioProposto(),
                    l.getTotalVigente(), l.getTotalProposto(), l.getVariacaoPercentual());
        }).toList();
        Snapshot snapshot = new Snapshot(caso.getNumero(), caso.getTipo().name(), caso.getEmpresaCodigo(),
                caso.getExercicio(), caso.getTipoAssembleia(), caso.getDataAssembleia(), totais, itens);
        try {
            return mapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar o snapshot da proposta.", e);
        }
    }

    private record Vigentes(BigDecimal honorario, BigDecimal beneficio) {
    }

    private record SnapshotLinha(String instancia, String cargo, int membros,
                                 BigDecimal honorarioVigente, BigDecimal honorarioProposto,
                                 BigDecimal beneficioVigente, BigDecimal beneficioProposto,
                                 BigDecimal totalVigente, BigDecimal totalProposto, BigDecimal variacao) {
    }

    private record Snapshot(String numero, String tipo, String empresa, int exercicio, String tipoAssembleia,
                            LocalDate dataAssembleia, Totais totais, List<SnapshotLinha> linhas) {
    }
}
