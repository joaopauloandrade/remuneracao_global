import { cookies } from 'next/headers';
import { NextResponse } from 'next/server';
import { COOKIE_NAMES, oidcConfig, requestTokens, safeReturnTo, stateMatches } from '@/lib/oidc';
import { definirTokens } from '@/lib/sessao-servidor';

export async function GET(request: Request) {
  const requestUrl = new URL(request.url);
  const store = await cookies();
  const code = requestUrl.searchParams.get('code');
  const state = requestUrl.searchParams.get('state');
  const verifier = store.get(COOKIE_NAMES.verifier)?.value;
  const expectedState = store.get(COOKIE_NAMES.state)?.value;
  const returnTo = safeReturnTo(store.get(COOKIE_NAMES.returnTo)?.value);

  for (const name of [COOKIE_NAMES.verifier, COOKIE_NAMES.state, COOKIE_NAMES.returnTo]) store.delete(name);

  if (!code || !verifier || !stateMatches(state, expectedState)) {
    return NextResponse.redirect(new URL('/sessao?erro=oidc', request.url));
  }

  try {
    const config = oidcConfig();
    const tokens = await requestTokens(new URLSearchParams({
      grant_type: 'authorization_code',
      client_id: config.clientId,
      code,
      code_verifier: verifier,
      redirect_uri: config.redirectUri,
    }));
    const response = NextResponse.redirect(new URL(returnTo, request.url));
    definirTokens(response, tokens);
    return response;
  } catch (error) {
    console.error('Falha no callback OIDC.', error);
    return NextResponse.redirect(new URL('/sessao?erro=oidc', request.url));
  }
}
