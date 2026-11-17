const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';
const CHAVE_TOKEN = 'rmg.token';

export function obterToken(): string | null {
  if (typeof window === 'undefined') return null;
  return window.sessionStorage.getItem(CHAVE_TOKEN);
}

export function definirToken(token: string | null): void {
  if (token) {
    window.sessionStorage.setItem(CHAVE_TOKEN, token);
  } else {
    window.sessionStorage.removeItem(CHAVE_TOKEN);
  }
}

export class ErroApi extends Error {
  constructor(
    readonly status: number,
    mensagem: string,
  ) {
    super(mensagem);
  }
}

async function requisitar(path: string, metodo: string, corpo?: unknown): Promise<Response> {
  const headers = new Headers();
  const token = obterToken();
  if (token) headers.set('Authorization', `Bearer ${token}`);
  if (corpo !== undefined) headers.set('Content-Type', 'application/json');

  const resp = await fetch(`${API_URL}${path}`, {
    method: metodo,
    headers,
    body: corpo === undefined ? undefined : JSON.stringify(corpo),
  });

  if (!resp.ok) {
    let mensagem = `Erro ${resp.status}`;
    try {
      const problema = (await resp.json()) as { detail?: string; title?: string };
      mensagem = problema.detail ?? problema.title ?? mensagem;
    } catch {
      // corpo sem JSON: mantém a mensagem padrão
    }
    throw new ErroApi(resp.status, mensagem);
  }
  return resp;
}

export async function api<T>(path: string, metodo: string = 'GET', corpo?: unknown): Promise<T> {
  const resp = await requisitar(path, metodo, corpo);
  if (resp.status === 204) return undefined as T;
  return (await resp.json()) as T;
}

export async function baixarArquivo(path: string, nomeArquivo: string): Promise<void> {
  const resp = await requisitar(path, 'GET');
  const url = URL.createObjectURL(await resp.blob());
  const link = document.createElement('a');
  link.href = url;
  link.download = nomeArquivo;
  link.click();
  URL.revokeObjectURL(url);
}
