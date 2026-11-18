import { tokenClaims } from '@/lib/oidc';
import { lerSessao } from '@/lib/sessao-servidor';

export async function GET() {
  const session = await lerSessao();
  if (!session.accessToken) return Response.json({ autenticado: false });
  const claims = tokenClaims(session.idToken);
  return Response.json({
    autenticado: true,
    usuario: claims?.preferred_username ?? claims?.name ?? claims?.sub ?? null,
  });
}
