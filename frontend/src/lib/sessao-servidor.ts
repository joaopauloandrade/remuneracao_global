import { cookies } from 'next/headers';
import type { NextResponse } from 'next/server';
import { COOKIE_NAMES, requestTokens, type OidcTokens } from './oidc';

const isProduction = process.env.NODE_ENV === 'production';
const cookieBase = {
  httpOnly: true,
  secure: isProduction,
  sameSite: 'lax' as const,
  path: '/',
};

export function definirTokens(response: NextResponse, tokens: OidcTokens) {
  const expires = new Date(Date.now() + (tokens.expires_in ?? 300) * 1000);
  response.cookies.set(COOKIE_NAMES.access, tokens.access_token, {
    ...cookieBase,
    expires,
  });
  response.cookies.set(COOKIE_NAMES.expires, String(expires.getTime()), cookieBase);
  if (tokens.refresh_token) {
    response.cookies.set(COOKIE_NAMES.refresh, tokens.refresh_token, cookieBase);
  }
  if (tokens.id_token) {
    response.cookies.set(COOKIE_NAMES.id, tokens.id_token, cookieBase);
  }
}

export function limparSessao(response: NextResponse) {
  Object.values(COOKIE_NAMES).forEach((name) => response.cookies.delete(name));
}

export async function obterAccessToken(): Promise<string | null> {
  const store = await cookies();
  const accessToken = store.get(COOKIE_NAMES.access)?.value;
  if (!accessToken) return null;
  const expiresAt = Number(store.get(COOKIE_NAMES.expires)?.value ?? 0);
  if (expiresAt > Date.now() + 30_000) return accessToken;

  const refreshToken = store.get(COOKIE_NAMES.refresh)?.value;
  if (!refreshToken) return accessToken;
  const tokens = await requestTokens(new URLSearchParams({
    grant_type: 'refresh_token',
    refresh_token: refreshToken,
  }));
  // O Route Handler que usa este helper persiste tokens renovados.
  const refreshedExpires = Date.now() + (tokens.expires_in ?? 300) * 1000;
  store.set(COOKIE_NAMES.access, tokens.access_token, { ...cookieBase, expires: new Date(refreshedExpires) });
  store.set(COOKIE_NAMES.expires, String(refreshedExpires), cookieBase);
  if (tokens.refresh_token) store.set(COOKIE_NAMES.refresh, tokens.refresh_token, cookieBase);
  if (tokens.id_token) store.set(COOKIE_NAMES.id, tokens.id_token, cookieBase);
  return tokens.access_token;
}

export async function lerSessao() {
  const store = await cookies();
  return {
    accessToken: store.get(COOKIE_NAMES.access)?.value,
    refreshToken: store.get(COOKIE_NAMES.refresh)?.value,
    idToken: store.get(COOKIE_NAMES.id)?.value,
  };
}
