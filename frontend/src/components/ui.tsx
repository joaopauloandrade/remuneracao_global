import Link from 'next/link';
import type { ButtonHTMLAttributes, ComponentPropsWithRef, ReactNode } from 'react';
import { SessaoUsuario } from './sessao-usuario';

export function Cabecalho({ titulo, acoes }: { titulo: string; acoes?: ReactNode }) {
  return (
    <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
      <h1 className="text-2xl font-semibold text-slate-900">{titulo}</h1>
      <div className="flex flex-wrap gap-2">{acoes}</div>
    </div>
  );
}

export function Botao({
  children,
  variante = 'primario',
  className = '',
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & { variante?: 'primario' | 'secundario' | 'perigo' }) {
  const estilos = {
    primario: 'bg-blue-700 text-white hover:bg-blue-800',
    secundario: 'border border-slate-300 bg-white text-slate-800 hover:bg-slate-50',
    perigo: 'bg-red-700 text-white hover:bg-red-800',
  }[variante];
  return (
    <button
      {...props}
      className={`rounded px-3 py-2 text-sm font-medium disabled:cursor-not-allowed disabled:opacity-50 ${estilos} ${className}`}
    >
      {children}
    </button>
  );
}

export function Mensagem({ texto, tipo = 'erro' }: { texto: string; tipo?: 'erro' | 'sucesso' }) {
  const estilos = tipo === 'erro' ? 'border-red-300 bg-red-50 text-red-800' : 'border-green-300 bg-green-50 text-green-800';
  return <div className={`mb-4 rounded border px-3 py-2 text-sm ${estilos}`} role={tipo === 'erro' ? 'alert' : 'status'}>{texto}</div>;
}

export function CampoTexto({
  rotulo,
  erro,
  ...props
}: ComponentPropsWithRef<'input'> & { rotulo: string; erro?: string }) {
  return (
    <label className="flex flex-col gap-1 text-sm text-slate-700">
      {rotulo}
      <input {...props} className="rounded border border-slate-300 px-2 py-1.5 text-slate-900" />
      {erro && <span className="text-xs text-red-700">{erro}</span>}
    </label>
  );
}

export function Navegacao() {
  return (
    <header className="border-b border-slate-200 bg-white">
      <nav className="mx-auto flex max-w-7xl flex-wrap items-center gap-4 px-4 py-3 text-sm">
        <span className="font-semibold text-blue-900">Remuneração Global</span>
        <Link href="/" className="text-slate-700 hover:text-blue-800">Painel</Link>
        <Link href="/casos" className="text-slate-700 hover:text-blue-800">Casos</Link>
        <Link href="/cadastros" className="text-slate-700 hover:text-blue-800">Cadastros</Link>
        <SessaoUsuario />
      </nav>
    </header>
  );
}
