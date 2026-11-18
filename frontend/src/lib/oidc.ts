import { createHash, randomBytes, timingSafeEqual } from 'node:crypto';

export type OidcMetadata = {
  authorization_endpoint: string;
  token_endpoint: string;
  end_session_endpoint?: string;
};

export type OidcTokens = {
  access_token: string;
  refresh_token?: string;
  id_token?: string;
  expires_in?: number;
};

const COOKIE_ACCESS = 'rmg_access_token';
const COOKIE_REFRESH = 'rmg_refresh_token';
const COOKIE_ID = 'rmg_id_token';
const COOKIE_EXPIRES = 'rmg_token_expires';
const COOKIE_VERIFIER = 'rmg_oidc_verifier';
const COOKIE_STATE = 'rmg_oidc_state';
const COOKIE_RETURN_TO = 'rmg_oidc_return_to';

export const COOKIE_NAMES = {
  access: COOKIE_ACCESS,
  refresh: COOKIE_REFRESH,
  id: COOKIE_ID,
  expires: COOKIE_EXPIRES,
  verifier: COOKIE_VERIFIER,
  state: COOKIE_STATE,
  returnTo: COOKIE_RETURN_TO,
};

export function oidcConfig() {
  const issuer = process.env.OIDC_ISSUER_URL;
  const clientId = process.env.OIDC_CLIENT_ID;
  const redirectUri = process.env.OIDC_REDIRECT_URI ?? 'http://localhost:3000/auth/callback';
  if (!issuer || !clientId) {
    throw new Error('Configure OIDC_ISSUER_URL e OIDC_CLIENT_ID.');
  }
  return {
    issuer: issuer.replace(/\/$/, ''),
    clientId,
    clientSecret: process.env.OIDC_CLIENT_SECRET,
    redirectUri,
    scope: process.env.OIDC_SCOPE ?? 'openid profile email',
  };
}

export async function discoverOidc(): Promise<OidcMetadata> {
  const config = oidcConfig();
  const response = await fetch(`${config.issuer}/.well-known/openid-configuration`, {
    cache: 'no-store',
  });
  if (!response.ok) throw new Error('Não foi possível localizar a configuração OIDC.');
  return (await response.json()) as OidcMetadata;
}

export function randomBase64Url(bytes = 32): string {
  return randomBytes(bytes).toString('base64url');
}

export function pkceChallenge(verifier: string): string {
  return createHash('sha256').update(verifier).digest('base64url');
}

export function stateMatches(received: string | null, stored: string | undefined): boolean {
  if (!received || !stored) return false;
  const a = Buffer.from(received);
  const b = Buffer.from(stored);
  return a.length === b.length && timingSafeEqual(a, b);
}

export function safeReturnTo(value: string | null | undefined): string {
  if (!value || !value.startsWith('/') || value.startsWith('//')) return '/';
  return value;
}

export function tokenRequestMetadata() {
  const config = oidcConfig();
  return {
    ...config,
    clientCredentials:
      config.clientSecret
        ? `Basic ${Buffer.from(`${config.clientId}:${config.clientSecret}`).toString('base64')}`
        : undefined,
  };
}

export async function requestTokens(body: URLSearchParams): Promise<OidcTokens> {
  const metadata = await discoverOidc();
  const config = tokenRequestMetadata();
  if (!config.clientCredentials) body.set('client_id', config.clientId);
  const response = await fetch(metadata.token_endpoint, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      ...(config.clientCredentials ? { Authorization: config.clientCredentials } : {}),
    },
    body,
    cache: 'no-store',
  });
  if (!response.ok) throw new Error('O provedor OIDC não autorizou a sessão.');
  return (await response.json()) as OidcTokens;
}

export function tokenClaims(idToken?: string): Record<string, unknown> | null {
  if (!idToken) return null;
  const parts = idToken.split('.');
  if (parts.length < 2) return null;
  try {
    return JSON.parse(Buffer.from(parts[1], 'base64url').toString('utf8')) as Record<string, unknown>;
  } catch {
    return null;
  }
}
