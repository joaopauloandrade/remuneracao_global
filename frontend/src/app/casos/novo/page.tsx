'use client';

import { Suspense } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { api, ErroApi } from '@/lib/api';
import { Botao, Cabecalho, CampoTexto, Mensagem } from '@/components/ui';
import type { CasoResumo, CasoTipo, Empresa } from '@/lib/tipos';

interface FormularioCaso {
  tipo: CasoTipo;
  empresaCodigo: string;
  exercicio: number;
  tipoAssembleia: string;
  dataAssembleia: string;
}

export default function PaginaNovoCaso() {
  return (
    <Suspense fallback={<p className="text-slate-600">Carregando…</p>}>
      <FormularioNovoCaso />
    </Suspense>
  );
}

function FormularioNovoCaso() {
  const roteador = useRouter();
  const { register, handleSubmit, formState: { errors } } = useForm<FormularioCaso>({
    defaultValues: { tipo: 'RMG', exercicio: new Date().getFullYear(), tipoAssembleia: 'AGO' },
  });

  const empresas = useQuery({
    queryKey: ['cadastros', 'empresas'],
    queryFn: () => api<Empresa[]>('/api/cadastros/empresas'),
  });

  const criar = useMutation({
    mutationFn: (dados: FormularioCaso) =>
      api<CasoResumo>('/api/casos', 'POST', {
        ...dados,
        dataAssembleia: dados.dataAssembleia || null,
        exercicio: Number(dados.exercicio),
      }),
    onSuccess: (caso) => roteador.push(`/casos/${caso.id}`),
  });

  return (
    <>
      <Cabecalho titulo="Novo caso" />
      {criar.error instanceof ErroApi && <Mensagem texto={criar.error.message} />}
      <form
        onSubmit={handleSubmit((dados) => criar.mutate(dados))}
        className="grid max-w-2xl gap-4 rounded border border-slate-200 bg-white p-4"
        noValidate
      >
        <label className="flex flex-col gap-1 text-sm text-slate-700">
          Tipo
          <select className="rounded border border-slate-300 px-2 py-1.5" {...register('tipo', { required: true })}>
            <option value="RMG">RMG – Remuneração Global</option>
            <option value="RMGS">RMGS – Remuneração Global Suplementar</option>
          </select>
        </label>
        <label className="flex flex-col gap-1 text-sm text-slate-700">
          Empresa
          <select
            className="rounded border border-slate-300 px-2 py-1.5"
            {...register('empresaCodigo', { required: 'Selecione uma empresa.' })}
          >
            <option value="">Selecione…</option>
            {empresas.data?.map((e) => (
              <option key={e.codigo} value={e.codigo}>{e.sigla} ({e.codigo})</option>
            ))}
          </select>
          {errors.empresaCodigo && <span className="text-xs text-red-700">{errors.empresaCodigo.message}</span>}
        </label>
        <CampoTexto
          rotulo="Exercício"
          type="number"
          erro={errors.exercicio && 'Informe um ano entre 2000 e 2100.'}
          {...register('exercicio', { required: true, min: 2000, max: 2100, valueAsNumber: true })}
        />
        <CampoTexto
          rotulo="Tipo de assembleia"
          erro={errors.tipoAssembleia && 'Informe o tipo de assembleia.'}
          {...register('tipoAssembleia', { required: true, maxLength: 10 })}
        />
        <CampoTexto rotulo="Data da assembleia" type="date" {...register('dataAssembleia')} />
        <div>
          <Botao type="submit" disabled={criar.isPending}>
            {criar.isPending ? 'Criando…' : 'Criar caso'}
          </Botao>
        </div>
      </form>
    </>
  );
}
