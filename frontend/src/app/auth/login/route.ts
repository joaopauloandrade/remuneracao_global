import { NextResponse } from 'next/server';
import { COOKIE_NAMES, discoverOidc, oidcConfig, pkceChallenge, randomBase64Url, safeReturnTo } from '@/lib/oidc';

export async function GET(request: Request) {
  try {
    const metadata = await discoverOidc();
    const config = oidcConfig();
    const verifier = randomBase64Url(48);
    const state = randomBase64Url();
    const url = new URL(metadata.authorization_endpoint);
    url.search = new URLSearchParams({
      client_id: config.clientId,
      redirect_uri: config.redirectUri,
      response_type: 'code',
      scope: config.scope,
      state,
      code_challenge: pkceChallenge(verifier),
      code_challenge_method: 'S256',
    }).toString();

    const response = NextResponse.redirect(url);
    const transient = { httpOnly: true, secure: process.env.NODE_ENV === 'production', sameSite: 'lax' as const, path: '/', maxAge: 600 };
    response.cookies.set(COOKIE_NAMES.verifier, verifier, transient);
    response.cookies.set(COOKIE_NAMES.state, state, transient);
    response.cookies.set(COOKIE_NAMES.returnTo, safeReturnTo(new URL(request.url).searchParams.get('returnTo')), transient);
    return response;
  } catch (error) {
    const destination = new URL('/sessao?erro=configuracao', request.url);
    console.error('Falha ao iniciar autenticação OIDC.', error);
    return NextResponse.redirect(destination);
  }
}
