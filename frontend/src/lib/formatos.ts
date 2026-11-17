const FORMATO_MOEDA = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const FORMATO_NUMERO = new Intl.NumberFormat('pt-BR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});
const FORMATO_DATA = new Intl.DateTimeFormat('pt-BR');

export function formatarMoeda(valor: string | number | null | undefined): string {
  return FORMATO_MOEDA.format(Number(valor ?? 0));
}

export function formatarPercentual(valor: string | number | null | undefined): string {
  return `${FORMATO_NUMERO.format(Number(valor ?? 0))}%`;
}

export function formatarData(valor: string | null | undefined): string {
  return valor ? FORMATO_DATA.format(new Date(valor)) : '—';
}
