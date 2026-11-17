'use client';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useParams } from 'next/navigation';
import { Suspense, useState } from 'react';
import { useForm } from 'react-hook-form';
import { api, baixarArquivo } from '@/lib/api';
import { Botao, Cabecalho, CampoTexto, Mensagem } from '@/components/ui';
import { formatarData, formatarMoeda, formatarPercentual } from '@/lib/formatos';
import {
  ROTULO_ACAO,
  ROTULO_ESTADO,
  type AcaoCaso,
  type Cargo,
  type CasoDetalhe,
  type EstadoCaso,
  type Instancia,
  type LinhaResposta,
} from '@/lib/tipos';

const EDITAVEIS: EstadoCaso[] = ['RASCUNHO', 'DEVOLVIDO', 'EM_ANALISE'];

const ACOES_POR_ESTADO: Record<EstadoCaso, AcaoCaso[]> = {
  RASCUNHO: ['ENVIAR', 'CANCELAR'],
  PROPOSTA_ENVIADA: ['ASSUMIR_ANALISE', 'APROVAR', 'CANCELAR'],
  EM_ANALISE: ['DEVOLVER', 'APROVAR', 'CANCELAR'],
  DEVOLVIDO: ['ENVIAR', 'CANCELAR'],
  APROVADO: [],
  CANCELADO: [],
};

interface CorpoLinha {
  instanciaId: number;
  cargoId: number;
  quantidadeMembros: number;
  honorarioProposto: number;
  beneficioProposto: number;
}

interface ValoresLinha {
  instanciaId: string;
  cargoId: string;
  quantidadeMembros: number;
  honorarioProposto: number;
  beneficioProposto: number;
}

interface ValoresCabecalho {
  tipoAssembleia: string;
  dataAssembleia: string;
}

export default function PaginaDetalheCaso() {
  return (
    <Suspense fallback={<p className="text-slate-600">Carregando…</p>}>
      <DetalheCaso />
    </Suspense>
  );
}

function DetalheCaso() {
  const { id } = useParams<{ id: string }>();
  const casoId = Number(id);
  const qc = useQueryClient();

  const [erro, setErro] = useState<string | null>(null);
  const [linhaEditando, setLinhaEditando] = useState<LinhaResposta | null>(null);
  const [novaLinha, setNovaLinha] = useState(false);
  const [textoAcao, setTextoAcao] = useState('');

  const consulta = useQuery({
    queryKey: ['caso', casoId],
    queryFn: () => api<CasoDetalhe>(`/api/casos/${casoId}`),
    enabled: Number.isFinite(casoId),
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
      setLinhaEditando(null);
      setNovaLinha(false);
      setTextoAcao('');
      void qc.invalidateQueries();
    },
    onError: (e: Error) => setErro(e.message),
  });

  if (consulta.isPending) return <p className="text-slate-600">Carregando…</p>;
  if (consulta.error) return <Mensagem texto={consulta.error.message} />;
  const dados = consulta.data;
  if (!dados) return null;

  const { caso, linhas, totais } = dados;
  const editavel = EDITAVEIS.includes(caso.estado);
  const acoes = ACOES_POR_ESTADO[caso.estado];
  const pendente = operacao.isPending;

  const executarAcao = (acao: AcaoCaso) => {
    if (acao === 'CANCELAR' && !window.confirm('Cancelar este caso?')) return;
    operacao.mutate({
      caminho: `/api/casos/${casoId}/acoes`,
      metodo: 'POST',
      corpo: { acao, justificativa: textoAcao || null, motivo: textoAcao || null },
    });
  };

  const exportar = async (formato: 'excel' | 'pdf') => {
    try {
      await baixarArquivo(
        `/api/casos/${casoId}/exportacao/${formato}`,
        `RMG-${caso.numero}.${formato === 'excel' ? 'xlsx' : 'pdf'}`,
      );
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Falha ao exportar.');
    }
  };

  return (
    <>
      <Cabecalho
        titulo={`Caso ${caso.numero}`}
        acoes={
          <>
            <Botao variante="secundario" onClick={() => exportar('excel')}>Excel</Botao>
            <Botao variante="secundario" onClick={() => exportar('pdf')}>Nota técnica (PDF)</Botao>
          </>
        }
      />
      {erro && <Mensagem texto={erro} />}

      <section className="mb-6 grid gap-3 rounded border border-slate-200 bg-white p-4 text-sm md:grid-cols-3">
        <div><span className="text-slate-500">Estado</span><div className="font-medium">{ROTULO_ESTADO[caso.estado]}</div></div>
        <div><span className="text-slate-500">Tipo</span><div className="font-medium">{caso.tipo}</div></div>
        <div><span className="text-slate-500">Empresa</span><div className="font-medium">{caso.empresaCodigo}</div></div>
        <div><span className="text-slate-500">Exercício</span><div className="font-medium">{caso.exercicio}</div></div>
        <div><span className="text-slate-500">Responsável</span><div className="font-medium">{caso.responsavel ?? '—'}</div></div>
        <div><span className="text-slate-500">Criado por</span><div className="font-medium">{caso.criadoPor}</div></div>
        <div><span className="text-slate-500">Remuneração vigente</span><div className="font-medium">{formatarMoeda(totais.vigente)}</div></div>
        <div><span className="text-slate-500">Remuneração proposta</span><div className="font-medium">{formatarMoeda(totais.proposto)}</div></div>
        <div><span className="text-slate-500">Variação</span><div className="font-medium">{formatarPercentual(totais.variacaoPercentual)}</div></div>
        {caso.justificativa && (
          <div className="md:col-span-3"><span className="text-slate-500">Justificativa</span><div>{caso.justificativa}</div></div>
        )}
      </section>

      <section className="mb-6">
        <h2 className="mb-2 text-lg font-semibold text-slate-900">Cabeçalho</h2>
        <CabecalhoForm
          key={`${caso.id}-${caso.atualizadoEm}`}
          tipoAssembleia={caso.tipoAssembleia}
          dataAssembleia={caso.dataAssembleia}
          editavel={editavel}
          pendente={pendente}
          onSalvar={(corpo) =>
            operacao.mutate({ caminho: `/api/casos/${casoId}/cabecalho`, metodo: 'PUT', corpo })
          }
        />
      </section>

      <section className="mb-6">
        <div className="mb-2 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-slate-900">Linhas</h2>
          {editavel && !novaLinha && (
            <Botao onClick={() => { setNovaLinha(true); setLinhaEditando(null); }}>Nova linha</Botao>
          )}
        </div>

        {editavel && novaLinha && instancias.data && cargos.data && (
          <div className="mb-4">
            <LinhaForm
              instancias={instancias.data}
              cargos={cargos.data}
              pendente={pendente}
              onCancelar={() => setNovaLinha(false)}
              onSalvar={(corpo) =>
                operacao.mutate({ caminho: `/api/casos/${casoId}/linhas`, metodo: 'POST', corpo })
              }
            />
          </div>
        )}

        <div className="overflow-x-auto rounded border border-slate-200 bg-white">
          <table className="min-w-full text-sm">
            <thead className="bg-slate-100 text-left text-slate-700">
              <tr>
                <th className="px-3 py-2">Instância</th>
                <th className="px-3 py-2">Cargo</th>
                <th className="px-3 py-2 text-right">Membros</th>
                <th className="px-3 py-2 text-right">Honorário vigente</th>
                <th className="px-3 py-2 text-right">Honorário proposto</th>
                <th className="px-3 py-2 text-right">Benefício proposto</th>
                <th className="px-3 py-2 text-right">Total proposto</th>
                <th className="px-3 py-2 text-right">Variação</th>
                {editavel && <th className="px-3 py-2">Ações</th>}
              </tr>
            </thead>
            <tbody>
              {linhas.length === 0 && (
                <tr><td colSpan={9} className="px-3 py-4 text-center text-slate-500">Nenhuma linha.</td></tr>
              )}
              {linhas.map((linha) => (
                <LinhaRegistro
                  key={linha.id}
                  linha={linha}
                  editavel={editavel}
                  editando={linhaEditando?.id === linha.id}
                  instancias={instancias.data ?? []}
                  cargos={cargos.data ?? []}
                  pendente={pendente}
                  onEditar={() => { setLinhaEditando(linha); setNovaLinha(false); }}
                  onCancelarEdicao={() => setLinhaEditando(null)}
                  onSalvar={(corpo) =>
                    operacao.mutate({
                      caminho: `/api/casos/${casoId}/linhas/${linha.id}`,
                      metodo: 'PUT',
                      corpo,
                    })
                  }
                  onExcluir={() => {
                    if (window.confirm('Excluir esta linha?')) {
                      operacao.mutate({ caminho: `/api/casos/${casoId}/linhas/${linha.id}`, metodo: 'DELETE' });
                    }
                  }}
                />
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {acoes.length > 0 && (
        <section className="rounded border border-slate-200 bg-white p-4">
          <h2 className="mb-2 text-lg font-semibold text-slate-900">Ações</h2>
          <label className="mb-3 flex flex-col gap-1 text-sm text-slate-700">
            Justificativa ou motivo (obrigatório para devolver; exigido ao enviar se a variação passar do limite)
            <textarea
              className="rounded border border-slate-300 px-2 py-1.5"
              rows={3}
              maxLength={1000}
              value={textoAcao}
              onChange={(e) => setTextoAcao(e.target.value)}
            />
          </label>
          <div className="flex flex-wrap gap-2">
            {acoes.map((acao) => (
              <Botao
                key={acao}
                variante={acao === 'CANCELAR' ? 'perigo' : acao === 'APROVAR' ? 'primario' : 'secundario'}
                disabled={pendente}
                onClick={() => executarAcao(acao)}
              >
                {ROTULO_ACAO[acao]}
              </Botao>
            ))}
          </div>
        </section>
      )}
    </>
  );
}

function CabecalhoForm({
  tipoAssembleia,
  dataAssembleia,
  editavel,
  pendente,
  onSalvar,
}: {
  tipoAssembleia: string;
  dataAssembleia: string | null;
  editavel: boolean;
  pendente: boolean;
  onSalvar: (corpo: { tipoAssembleia: string; dataAssembleia: string | null }) => void;
}) {
  const { register, handleSubmit, formState: { errors } } = useForm<ValoresCabecalho>({
    defaultValues: { tipoAssembleia, dataAssembleia: dataAssembleia ?? '' },
  });

  if (!editavel) {
    return (
      <p className="text-sm text-slate-700">
        Assembleia {tipoAssembleia} em {formatarData(dataAssembleia)}
      </p>
    );
  }

  return (
    <form
      noValidate
      onSubmit={handleSubmit((v) => onSalvar({ tipoAssembleia: v.tipoAssembleia, dataAssembleia: v.dataAssembleia || null }))}
      className="grid max-w-xl gap-3 rounded border border-slate-200 bg-white p-4 md:grid-cols-3 md:items-end"
    >
      <CampoTexto
        rotulo="Tipo de assembleia"
        erro={errors.tipoAssembleia && 'Informe o tipo de assembleia.'}
        {...register('tipoAssembleia', { required: true, maxLength: 10 })}
      />
      <CampoTexto rotulo="Data da assembleia" type="date" {...register('dataAssembleia')} />
      <div>
        <Botao type="submit" variante="secundario" disabled={pendente}>Salvar cabeçalho</Botao>
      </div>
    </form>
  );
}

function LinhaRegistro({
  linha,
  editavel,
  editando,
  instancias,
  cargos,
  pendente,
  onEditar,
  onCancelarEdicao,
  onSalvar,
  onExcluir,
}: {
  linha: LinhaResposta;
  editavel: boolean;
  editando: boolean;
  instancias: Instancia[];
  cargos: Cargo[];
  pendente: boolean;
  onEditar: () => void;
  onCancelarEdicao: () => void;
  onSalvar: (corpo: CorpoLinha) => void;
  onExcluir: () => void;
}) {
  if (editando) {
    return (
      <tr>
        <td colSpan={9} className="p-3">
          <LinhaForm
            inicial={linha}
            instancias={instancias}
            cargos={cargos}
            pendente={pendente}
            onCancelar={onCancelarEdicao}
            onSalvar={onSalvar}
          />
        </td>
      </tr>
    );
  }
  return (
    <tr className="border-t border-slate-200">
      <td className="px-3 py-2">{linha.instancia}</td>
      <td className="px-3 py-2">{linha.cargo}</td>
      <td className="px-3 py-2 text-right">{linha.quantidadeMembros}</td>
      <td className="px-3 py-2 text-right">{formatarMoeda(linha.honorarioVigente)}</td>
      <td className="px-3 py-2 text-right">{formatarMoeda(linha.honorarioProposto)}</td>
      <td className="px-3 py-2 text-right">{formatarMoeda(linha.beneficioProposto)}</td>
      <td className="px-3 py-2 text-right">{formatarMoeda(linha.totalProposto)}</td>
      <td className="px-3 py-2 text-right">{formatarPercentual(linha.variacaoPercentual)}</td>
      {editavel && (
        <td className="px-3 py-2">
          <div className="flex gap-2">
            <button type="button" className="text-blue-800 hover:underline" onClick={onEditar}>Editar</button>
            <button type="button" className="text-red-700 hover:underline" onClick={onExcluir}>Excluir</button>
          </div>
        </td>
      )}
    </tr>
  );
}

function LinhaForm({
  inicial,
  instancias,
  cargos,
  pendente,
  onSalvar,
  onCancelar,
}: {
  inicial?: LinhaResposta;
  instancias: Instancia[];
  cargos: Cargo[];
  pendente: boolean;
  onSalvar: (corpo: CorpoLinha) => void;
  onCancelar: () => void;
}) {
  const { register, handleSubmit, formState: { errors } } = useForm<ValoresLinha>({
    defaultValues: {
      instanciaId: inicial ? String(inicial.instanciaId) : '',
      cargoId: inicial ? String(inicial.cargoId) : '',
      quantidadeMembros: inicial?.quantidadeMembros ?? 1,
      honorarioProposto: inicial ? Number(inicial.honorarioProposto) : 0,
      beneficioProposto: inicial ? Number(inicial.beneficioProposto) : 0,
    },
  });

  return (
    <form
      noValidate
      onSubmit={handleSubmit((v) =>
        onSalvar({
          instanciaId: Number(v.instanciaId),
          cargoId: Number(v.cargoId),
          quantidadeMembros: Number(v.quantidadeMembros),
          honorarioProposto: Number(v.honorarioProposto),
          beneficioProposto: Number(v.beneficioProposto),
        }),
      )}
      className="grid gap-3 rounded border border-slate-200 bg-slate-50 p-4 md:grid-cols-5 md:items-end"
    >
      <label className="flex flex-col gap-1 text-sm text-slate-700">
        Instância
        <select
          className="rounded border border-slate-300 px-2 py-1.5"
          {...register('instanciaId', { required: 'Selecione.' })}
        >
          <option value="">Selecione…</option>
          {instancias.map((i) => <option key={i.id} value={i.id}>{i.nome}</option>)}
        </select>
        {errors.instanciaId && <span className="text-xs text-red-700">{errors.instanciaId.message}</span>}
      </label>
      <label className="flex flex-col gap-1 text-sm text-slate-700">
        Cargo
        <select className="rounded border border-slate-300 px-2 py-1.5" {...register('cargoId', { required: 'Selecione.' })}>
          <option value="">Selecione…</option>
          {cargos.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
        </select>
        {errors.cargoId && <span className="text-xs text-red-700">{errors.cargoId.message}</span>}
      </label>
      <CampoTexto
        rotulo="Membros"
        type="number"
        min={1}
        erro={errors.quantidadeMembros && 'Mínimo 1.'}
        {...register('quantidadeMembros', { required: true, min: 1, valueAsNumber: true })}
      />
      <CampoTexto
        rotulo="Honorário proposto"
        type="number"
        step="0.01"
        min={0}
        erro={errors.honorarioProposto && 'Valor não negativo.'}
        {...register('honorarioProposto', { required: true, min: 0, valueAsNumber: true })}
      />
      <CampoTexto
        rotulo="Benefício proposto"
        type="number"
        step="0.01"
        min={0}
        erro={errors.beneficioProposto && 'Valor não negativo.'}
        {...register('beneficioProposto', { required: true, min: 0, valueAsNumber: true })}
      />
      <div className="flex gap-2 md:col-span-5">
        <Botao type="submit" disabled={pendente}>{inicial ? 'Salvar linha' : 'Adicionar linha'}</Botao>
        <Botao type="button" variante="secundario" onClick={onCancelar}>Cancelar</Botao>
      </div>
    </form>
  );
}
