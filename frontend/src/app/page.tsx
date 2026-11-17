'use client';

import { useQuery } from '@tanstack/react-query';
import Link from 'next/link';
import { api } from '@/lib/api';
import { Cabecalho, Mensagem } from '@/components/ui';
import { ROTULO_ESTADO, type EstadoCaso } from '@/lib/tipos';

export default function PaginaPainel() {
  const { data, error, isLoading } = useQuery({
    queryKey: ['dashboard', 'resumo'],
    queryFn: () => api<Partial<Record<EstadoCaso, number>>>('/api/dashboard/resumo'),
  });

  return (
    <>
      <Cabecalho
        titulo="Painel"
        acoes={<Link className="rounded bg-blue-700 px-3 py-2 text-sm font-medium text-white hover:bg-blue-800" href="/casos/novo">Novo caso</Link>}
      />
      {error && <Mensagem texto={error.message} />}
      {isLoading && <p className="text-slate-500">Carregando…</p>}
      {data && (
        <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-6">
          {(Object.keys(ROTULO_ESTADO) as EstadoCaso[]).map((estado) => (
            <Link
              key={estado}
              href={`/casos?estado=${estado}`}
              className="rounded border border-slate-200 bg-white p-4 hover:border-blue-400"
            >
              <div className="text-3xl font-semibold text-slate-900">{data[estado] ?? 0}</div>
              <div className="text-sm text-slate-600">{ROTULO_ESTADO[estado]}</div>
            </Link>
          ))}
        </div>
      )}
    </>
  );
}
