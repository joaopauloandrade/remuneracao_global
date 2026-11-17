'use client';

import { useQuery } from '@tanstack/react-query';
import Link from 'next/link';
import { useSearchParams } from 'next/navigation';
import { Suspense, useState } from 'react';
import { api } from '@/lib/api';
import { Cabecalho, Mensagem } from '@/components/ui';
import { formatarData } from '@/lib/formatos';
import { ROTULO_ESTADO, type CasoResumo, type EstadoCaso } from '@/lib/tipos';

function ListaCasos() {
  const parametros = useSearchParams();
  const [minhas, setMinhas] = useState(false);
  const estado = parametros.get('estado') as EstadoCaso | null;

  const { data, error, isLoading } = useQuery({
    queryKey: ['casos', { minhas }],
    queryFn: () => api<CasoResumo[]>(`/api/casos${minhas ? '?minhas=true' : ''}`),
  });

  const casos = (data ?? []).filter((c) => !estado || c.estado === estado);

  return (
    <>
      <Cabecalho
        titulo="Casos"
        acoes={
          <>
            <label className="flex items-center gap-2 text-sm text-slate-700">
              <input type="checkbox" checked={minhas} onChange={(e) => setMinhas(e.target.checked)} />
              Somente meus casos
            </label>
            <Link className="rounded bg-blue-700 px-3 py-2 text-sm font-medium text-white hover:bg-blue-800" href="/casos/novo">Novo caso</Link>
          </>
        }
      />
      {estado && <p className="mb-3 text-sm text-slate-600">Filtrando por: {ROTULO_ESTADO[estado]}</p>}
      {error && <Mensagem texto={error.message} />}
      {isLoading && <p className="text-slate-500">Carregando…</p>}
      {!isLoading && casos.length === 0 && <p className="text-slate-500">Nenhum caso encontrado.</p>}
      {casos.length > 0 && (
        <div className="overflow-x-auto rounded border border-slate-200 bg-white">
          <table className="min-w-full text-left text-sm">
            <thead className="bg-slate-100 text-slate-700">
              <tr>
                <th className="px-3 py-2">Número</th>
                <th className="px-3 py-2">Tipo</th>
                <th className="px-3 py-2">Empresa</th>
                <th className="px-3 py-2">Exercício</th>
                <th className="px-3 py-2">Estado</th>
                <th className="px-3 py-2">Responsável</th>
                <th className="px-3 py-2">Atualizado em</th>
              </tr>
            </thead>
            <tbody>
              {casos.map((c) => (
                <tr key={c.id} className="border-t border-slate-100 hover:bg-slate-50">
                  <td className="px-3 py-2"><Link className="text-blue-700 underline" href={`/casos/${c.id}`}>{c.numero}</Link></td>
                  <td className="px-3 py-2">{c.tipo}</td>
                  <td className="px-3 py-2">{c.empresaCodigo}</td>
                  <td className="px-3 py-2">{c.exercicio}</td>
                  <td className="px-3 py-2">{ROTULO_ESTADO[c.estado]}</td>
                  <td className="px-3 py-2">{c.responsavel ?? '—'}</td>
                  <td className="px-3 py-2">{formatarData(c.atualizadoEm)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}

export default function PaginaCasos() {
  return (
    <Suspense fallback={<p className="text-slate-500">Carregando…</p>}>
      <ListaCasos />
    </Suspense>
  );
}
