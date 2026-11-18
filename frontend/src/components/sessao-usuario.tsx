'use client';

import { useEffect, useState } from 'react';

type Sessao = { autenticado: boolean; usuario?: string | null };

export function SessaoUsuario() {
  const [sessao, setSessao] = useState<Sessao | null>(null);

  useEffect(() => {
    void fetch('/auth/session', { cache: 'no-store' })
      .then((response) => response.json() as Promise<Sessao>)
      .then(setSessao)
      .catch(() => setSessao({ autenticado: false }));
  }, []);

  if (!sessao) return <span className="ml-auto text-slate-400">Verificando sessão…</span>;
  if (!sessao.autenticado) {
    return <a href="/auth/login" className="ml-auto font-medium text-blue-700 hover:text-blue-900">Entrar</a>;
  }
  return (
    <div className="ml-auto flex items-center gap-3">
      {sessao.usuario && <span className="max-w-48 truncate text-slate-600" title={sessao.usuario}>{sessao.usuario}</span>}
      <a href="/auth/logout" className="text-slate-500 hover:text-blue-800">Sair</a>
    </div>
  );
}
