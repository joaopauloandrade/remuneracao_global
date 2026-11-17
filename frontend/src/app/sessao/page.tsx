'use client';

import { useState } from 'react';
import { definirToken, obterToken } from '@/lib/api';
import { Botao, Cabecalho, Mensagem } from '@/components/ui';

export default function PaginaSessao() {
  const [token, setToken] = useState(() => obterToken() ?? '');
  const [salvo, setSalvo] = useState(false);

  return (
    <>
      <Cabecalho titulo="Sessão" />
      <div className="max-w-2xl space-y-4 rounded border border-slate-200 bg-white p-4">
        <p className="text-sm text-slate-600">
          Informe o token de acesso (JWT) emitido pelo provedor OIDC. Ele fica apenas na sessão do navegador.
          A integração de login automático ainda não está implementada.
        </p>
        {salvo && <Mensagem tipo="sucesso" texto="Token salvo para esta sessão." />}
        <textarea
          className="h-32 w-full rounded border border-slate-300 p-2 font-mono text-xs"
          value={token}
          onChange={(e) => {
            setToken(e.target.value);
            setSalvo(false);
          }}
          aria-label="Token JWT"
        />
        <div className="flex gap-2">
          <Botao onClick={() => { definirToken(token.trim() || null); setSalvo(true); }}>Salvar</Botao>
          <Botao variante="secundario" onClick={() => { definirToken(null); setToken(''); setSalvo(false); }}>
            Limpar
          </Botao>
        </div>
      </div>
    </>
  );
}
