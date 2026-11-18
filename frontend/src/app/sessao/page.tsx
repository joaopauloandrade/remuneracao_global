import { Cabecalho, Mensagem } from '@/components/ui';

type Props = { searchParams: Promise<{ erro?: string; logout?: string }> };

export const instant = false;

export default async function PaginaSessao({ searchParams }: Props) {
  const parametros = await searchParams;
  return (
    <>
      <Cabecalho titulo="Sessão" />
      {parametros.logout === '1' && <Mensagem tipo="sucesso" texto="Sessão encerrada com sucesso." />}
      {parametros.erro === 'configuracao' && <Mensagem texto="A autenticação OIDC não está configurada corretamente." />}
      {parametros.erro === 'oidc' && <Mensagem texto="Não foi possível concluir a autenticação. Tente novamente." />}
      <div className="max-w-2xl space-y-4 rounded border border-slate-200 bg-white p-5">
        <p className="text-sm text-slate-600">
          Entre com a conta institucional. Você será redirecionado ao provedor de identidade e retornará automaticamente ao sistema.
        </p>
        <a className="inline-block rounded bg-blue-700 px-4 py-2 text-sm font-medium text-white hover:bg-blue-800" href="/auth/login">
          Entrar com conta institucional
        </a>
      </div>
    </>
  );
}
