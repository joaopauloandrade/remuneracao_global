import { NextResponse } from 'next/server';
import { discoverOidc, oidcConfig } from '@/lib/oidc';
import { lerSessao, limparSessao } from '@/lib/sessao-servidor';

export async function GET(request: Request) {
  const session = await lerSessao();
  const origin = new URL(request.url).origin;
  let destination = new URL('/sessao?logout=1', request.url);
  try {
    const metadata = await discoverOidc();
    if (metadata.end_session_endpoint) {
      destination = new URL(metadata.end_session_endpoint);
      destination.searchParams.set('post_logout_redirect_uri', origin);
      if (session.idToken) destination.searchParams.set('id_token_hint', session.idToken);
      destination.searchParams.set('client_id', oidcConfig().clientId);
    }
  } catch (error) {
    console.error('Falha ao encerrar sessão no provedor OIDC.', error);
  }
  const response = NextResponse.redirect(destination);
  limparSessao(response);
  return response;
}
