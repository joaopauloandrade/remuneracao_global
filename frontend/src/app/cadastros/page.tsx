'use client';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { api } from '@/lib/api';
import { Botao, Cabecalho, CampoTexto, Mensagem } from '@/components/ui';
import type { Cargo, Empresa, Instancia } from '@/lib/tipos';
import { useState } from 'react';
import type { ReactNode } from 'react';

interface FormularioInstancia {
  codigo: string;
  nome: string;
  descricao: string;
}

interface FormularioCargo {
  codigo: string;
  nome: string;
}

export default function PaginaCadastros() {
  const qc = useQueryClient();
  const [erro, setErro] = useState<string | null>(null);

  const empresas = useQuery({
    queryKey: ['cadastros', 'empresas'],
    queryFn: () => api<Empresa[]>('/api/cadastros/empresas'),
  });
  const instancias = useQuery({
    queryKey: ['cadastros', 'instancias'],
    queryFn: () => api<Instancia[]>('/api/cadastros/instancias'),
  });
  const cargos = useQuery({
    queryKey: ['cadastros', 'cargos'],
    queryFn: () => api<Cargo[]>('/api/cadastros/cargos'),
  });

  const operacao = useMutation({
    mutationFn: ({ caminho, metodo, corpo }: { caminho: string; metodo: string; corpo?: unknown }) =>
      api<unknown>(caminho, metodo, corpo),
    onSuccess: () => {
      setErro(null);
      void qc.invalidateQueries({ queryKey: ['cadastros'] });
    },
    onError: (e: Error) => setErro(e.message),
  });

  const formInstancia = useForm<FormularioInstancia>({ defaultValues: { codigo: '', nome: '', descricao: '' } });
  const formCargo = useForm<FormularioCargo>({ defaultValues: { codigo: '', nome: '' } });

  const renomear = (caminho: string, atual: string, pedirDescricao: boolean, descricaoAtual: string | null) => {
    const nome = window.prompt('Novo nome:', atual);
    if (!nome || nome === atual && !pedirDescricao) return;
    const corpo = pedirDescricao
      ? { nome, descricao: window.prompt('Descrição:', descricaoAtual ?? '') || null }
      : { nome };
    operacao.mutate({ caminho, metodo: 'PUT', corpo });
  };

  const desativar = (caminho: string) => {
    const motivo = window.prompt('Motivo da desativação:');
    if (!motivo) return;
    operacao.mutate({ caminho, metodo: 'POST', corpo: { motivo } });
  };

  return (
    <>
      <Cabecalho titulo="Cadastros" />
      {erro && <Mensagem texto={erro} />}

      <section className="mb-8">
        <h2 className="mb-2 text-lg font-semibold text-slate-900">Empresas</h2>
        <p className="mb-2 text-sm text-slate-500">Somente leitura: carregadas do cadastro de referência.</p>
        {empresas.error && <Mensagem texto={empresas.error.message} />}
        <Tabela
          cabecalhos={['Código', 'Sigla', 'Setor', 'Porte', 'Controle', 'Controladora']}
          linhas={(empresas.data ?? []).map((e) => [
            e.codigo, e.sigla, e.setor, e.porte, e.controle, e.controladora ? 'Sim' : 'Não',
          ])}
        />
      </section>

      <section className="mb-8">
        <h2 className="mb-2 text-lg font-semibold text-slate-900">Instâncias</h2>
        {instancias.error && <Mensagem texto={instancias.error.message} />}
        <form
          noValidate
          onSubmit={formInstancia.handleSubmit((v) => {
            operacao.mutate({
              caminho: '/api/cadastros/instancias',
              metodo: 'POST',
              corpo: { codigo: v.codigo, nome: v.nome, descricao: v.descricao || null },
            });
            formInstancia.reset();
          })}
          className="mb-3 grid max-w-3xl gap-3 md:grid-cols-4 md:items-end"
        >
          <CampoTexto rotulo="Código" erro={formInstancia.formState.errors.codigo && 'Informe o código.'}
            {...formInstancia.register('codigo', { required: true, maxLength: 20 })} />
          <CampoTexto rotulo="Nome" erro={formInstancia.formState.errors.nome && 'Informe o nome.'}
            {...formInstancia.register('nome', { required: true, maxLength: 200 })} />
          <CampoTexto rotulo="Descrição" {...formInstancia.register('descricao', { maxLength: 1000 })} />
          <Botao type="submit" disabled={operacao.isPending}>Criar instância</Botao>
        </form>
        <Tabela
          cabecalhos={['Código', 'Nome', 'Descrição', 'Situação', 'Ações']}
          linhas={(instancias.data ?? []).map((i) => [
            i.codigo,
            i.nome,
            i.descricao ?? '—',
            i.ativo ? 'Ativa' : 'Desativada',
            i.ativo ? (
              <AcoesLinha
                key={i.id}
                onEditar={() => renomear(`/api/cadastros/instancias/${i.id}`, i.nome, true, i.descricao)}
                onDesativar={() => desativar(`/api/cadastros/instancias/${i.id}/desativacao`)}
              />
            ) : '—',
          ])}
        />
      </section>

      <section className="mb-8">
        <h2 className="mb-2 text-lg font-semibold text-slate-900">Cargos</h2>
        {cargos.error && <Mensagem texto={cargos.error.message} />}
        <form
          noValidate
          onSubmit={formCargo.handleSubmit((v) => {
            operacao.mutate({ caminho: '/api/cadastros/cargos', metodo: 'POST', corpo: v });
            formCargo.reset();
          })}
          className="mb-3 grid max-w-3xl gap-3 md:grid-cols-3 md:items-end"
        >
          <CampoTexto rotulo="Código" erro={formCargo.formState.errors.codigo && 'Informe o código.'}
            {...formCargo.register('codigo', { required: true, maxLength: 20 })} />
          <CampoTexto rotulo="Nome" erro={formCargo.formState.errors.nome && 'Informe o nome.'}
            {...formCargo.register('nome', { required: true, maxLength: 200 })} />
          <Botao type="submit" disabled={operacao.isPending}>Criar cargo</Botao>
        </form>
        <Tabela
          cabecalhos={['Código', 'Nome', 'Situação', 'Ações']}
          linhas={(cargos.data ?? []).map((c) => [
            c.codigo,
            c.nome,
            c.ativo ? 'Ativo' : 'Desativado',
            c.ativo ? (
              <AcoesLinha
                key={c.id}
                onEditar={() => renomear(`/api/cadastros/cargos/${c.id}`, c.nome, false, null)}
                onDesativar={() => desativar(`/api/cadastros/cargos/${c.id}/desativacao`)}
              />
            ) : '—',
          ])}
        />
      </section>
    </>
  );
}

function AcoesLinha({ onEditar, onDesativar }: { onEditar: () => void; onDesativar: () => void }) {
  return (
    <div className="flex gap-2">
      <button type="button" className="text-blue-800 hover:underline" onClick={onEditar}>Editar</button>
      <button type="button" className="text-red-700 hover:underline" onClick={onDesativar}>Desativar</button>
    </div>
  );
}

function Tabela({ cabecalhos, linhas }: { cabecalhos: string[]; linhas: ReactNode[][] }) {
  return (
    <div className="overflow-x-auto rounded border border-slate-200 bg-white">
      <table className="min-w-full text-sm">
        <thead className="bg-slate-100 text-left text-slate-700">
          <tr>
            {cabecalhos.map((c) => <th key={c} className="px-3 py-2">{c}</th>)}
          </tr>
        </thead>
        <tbody>
          {linhas.length === 0 && (
            <tr><td colSpan={cabecalhos.length} className="px-3 py-4 text-center text-slate-500">Nenhum registro.</td></tr>
          )}
          {linhas.map((celulas, i) => (
            <tr key={i} className="border-t border-slate-200">
              {celulas.map((celula, j) => <td key={j} className="px-3 py-2">{celula}</td>)}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
