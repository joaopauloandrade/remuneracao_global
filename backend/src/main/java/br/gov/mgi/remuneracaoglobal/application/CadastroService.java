package br.gov.mgi.remuneracaoglobal.application;

import br.gov.mgi.remuneracaoglobal.domain.RecursoNaoEncontradoException;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.CargoEntity;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.CargoRepository;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.EmpresaEntity;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.EmpresaRepository;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.InstanciaGestaoEntity;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.InstanciaGestaoRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cadastros de referência. Empresas são somente leitura; instâncias e cargos são administrados por ADMIN. */
@Service
public class CadastroService {

    private final InstanciaGestaoRepository instancias;
    private final CargoRepository cargos;
    private final EmpresaRepository empresas;
    private final AuditoriaService auditoria;

    public CadastroService(InstanciaGestaoRepository instancias, CargoRepository cargos,
                           EmpresaRepository empresas, AuditoriaService auditoria) {
        this.instancias = instancias;
        this.cargos = cargos;
        this.empresas = empresas;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<EmpresaEntity> listarEmpresas() {
        return empresas.findAllByOrderByCodigoAsc();
    }

    @Transactional(readOnly = true)
    public List<InstanciaGestaoEntity> listarInstancias() {
        return instancias.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<CargoEntity> listarCargos() {
        return cargos.findAllByOrderByNomeAsc();
    }

    @Transactional
    public InstanciaGestaoEntity criarInstancia(ContextoUsuario ctx, String codigo, String nome, String descricao) {
        exigirAdmin(ctx);
        InstanciaGestaoEntity instancia = instancias.saveAndFlush(new InstanciaGestaoEntity(codigo, nome, descricao));
        auditoria.registrar(ctx.usuario(), "CRIAR", "INSTANCIA_GESTAO", String.valueOf(instancia.getId()), codigo);
        return instancia;
    }

    @Transactional
    public InstanciaGestaoEntity atualizarInstancia(ContextoUsuario ctx, Long id, String nome, String descricao) {
        exigirAdmin(ctx);
        InstanciaGestaoEntity instancia = instancias.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Instância " + id + " não encontrada."));
        instancia.atualizar(nome, descricao);
        auditoria.registrar(ctx.usuario(), "ATUALIZAR", "INSTANCIA_GESTAO", String.valueOf(id), nome);
        return instancia;
    }

    @Transactional
    public void desativarInstancia(ContextoUsuario ctx, Long id, String motivo) {
        exigirAdmin(ctx);
        InstanciaGestaoEntity instancia = instancias.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Instância " + id + " não encontrada."));
        instancia.desativar(motivo, LocalDateTime.now());
        auditoria.registrar(ctx.usuario(), "DESATIVAR", "INSTANCIA_GESTAO", String.valueOf(id), motivo);
    }

    @Transactional
    public CargoEntity criarCargo(ContextoUsuario ctx, String codigo, String nome) {
        exigirAdmin(ctx);
        CargoEntity cargo = cargos.saveAndFlush(new CargoEntity(codigo, nome));
        auditoria.registrar(ctx.usuario(), "CRIAR", "CARGO", String.valueOf(cargo.getId()), codigo);
        return cargo;
    }

    @Transactional
    public CargoEntity atualizarCargo(ContextoUsuario ctx, Long id, String nome) {
        exigirAdmin(ctx);
        CargoEntity cargo = cargos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cargo " + id + " não encontrado."));
        cargo.atualizar(nome);
        cargos.flush();
        auditoria.registrar(ctx.usuario(), "ATUALIZAR", "CARGO", String.valueOf(id), nome);
        return cargo;
    }

    @Transactional
    public void desativarCargo(ContextoUsuario ctx, Long id, String motivo) {
        exigirAdmin(ctx);
        CargoEntity cargo = cargos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cargo " + id + " não encontrado."));
        cargo.desativar(motivo, LocalDateTime.now());
        auditoria.registrar(ctx.usuario(), "DESATIVAR", "CARGO", String.valueOf(id), motivo);
    }

    private void exigirAdmin(ContextoUsuario ctx) {
        if (!ctx.admin()) {
            throw new AcessoNegadoException("Somente administradores podem alterar cadastros.");
        }
    }
}
