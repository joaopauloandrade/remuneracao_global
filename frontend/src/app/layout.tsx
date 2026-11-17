import type { Metadata } from 'next';
import type { ReactNode } from 'react';
import './globals.css';
import { Navegacao } from '@/components/ui';
import { Providers } from '@/components/providers';

export const metadata: Metadata = {
  title: 'Remuneração Global',
  description: 'Gestão das propostas de remuneração global das empresas estatais',
};

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang="pt-BR">
      <body className="min-h-screen bg-slate-50 font-sans antialiased">
        <Providers>
          <Navegacao />
          <main className="mx-auto max-w-7xl px-4 py-6">{children}</main>
        </Providers>
      </body>
    </html>
  );
}
