# Keycloak local — Remuneração Global

## Pré-requisito

Instale o Docker Desktop para Windows e habilite o backend Linux Containers.

## Subir

Na raiz do projeto:

```powershell
docker compose -f compose.keycloak.yml up -d
```

Acompanhar a inicialização:

```powershell
docker compose -f compose.keycloak.yml logs -f keycloak
```

Verificar o endpoint OIDC:

http://localhost:8180/realms/rmg/.well-known/openid-configuration

## Acessos locais

Console administrativo: http://localhost:8180/admin

- Administrador do Keycloak: `admin` / `admin`
- Admin RMG: `admin.rmg` / `admin123`
- Preenchedor: `preenchedor.rmg` / `preenchedor123`
- Técnico: `tecnico.rmg` / `tecnico123`
- Gestor: `gestor.rmg` / `gestor123`

Após subir o Keycloak, reinicie o frontend (`npm run dev`) para carregar `.env.local`.

O login é iniciado em http://localhost:3000/auth/login.

## Parar

```powershell
docker compose -f compose.keycloak.yml down
```

## Resetar importação do realm

O Keycloak importa arquivos somente quando o realm ainda não existe. Para recriar o realm a partir do JSON:

```powershell
docker compose -f compose.keycloak.yml down -v
docker compose -f compose.keycloak.yml up -d
```

> Ambiente exclusivamente local. Altere todas as senhas antes de qualquer uso compartilhado ou remoto.
