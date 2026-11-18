import { obterAccessToken } from '@/lib/sessao-servidor';

const BACKEND_URL = process.env.RMG_API_URL ?? 'http://localhost:8080';

type Context = { params: Promise<{ path: string[] }> };

async function encaminhar(request: Request, context: Context) {
  const token = await obterAccessToken();
  if (!token) return Response.json({ title: 'Não autenticado' }, { status: 401 });
  const { path } = await context.params;
  const requestUrl = new URL(request.url);
  const destination = new URL(`/api/${path.map(encodeURIComponent).join('/')}`, BACKEND_URL);
  destination.search = requestUrl.search;
  const headers = new Headers();
  const contentType = request.headers.get('content-type');
  if (contentType) headers.set('Content-Type', contentType);
  headers.set('Authorization', `Bearer ${token}`);
  headers.set('Accept', request.headers.get('accept') ?? 'application/json');
  const body = request.method === 'GET' || request.method === 'HEAD' ? undefined : await request.arrayBuffer();
  return fetch(destination, {
    method: request.method,
    headers,
    body,
    cache: 'no-store',
    redirect: 'manual',
  });
}

export const GET = encaminhar;
export const POST = encaminhar;
export const PUT = encaminhar;
export const DELETE = encaminhar;
export const PATCH = encaminhar;
