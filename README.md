# oauth-integration-api

Production-oriented OAuth/OIDC integration harness using Spring Boot OAuth2 Client and BFF (server-side auth/session) pattern.

## Architecture

- UI: `https://auth-ui.bondbenbond.com`
- API: `https://auth-api.bondbenbond.com`
- Login starts at: `GET /oauth2/authorization/{provider}`
- Callback: `GET /login/oauth2/code/{provider}`
- API creates HTTP session and redirects to UI root
- UI calls API with `credentials: include`

## Endpoints (v1)

- `GET /api/health` -> `{ "status": "ok" }`
- `GET /api/whoami` -> normalized identity
- `GET /api/providers` -> configured providers + capabilities
- `POST /api/logout` -> `204`
- `POST /api/test/call` -> provider userinfo/configured resource call

## Security choices

- No secrets in repo. Use env vars and local profile overrides.
- Access/refresh token values are never returned unless both are true:
  - `local` profile active
  - `DEBUG_TOKENS=true`
- CORS explicitly allows one UI origin (`app.allowed-origin`) and credentials.
- CSRF is enabled with cookie token (`XSRF-TOKEN`) and header `X-XSRF-TOKEN` required for POST.
- Session cookie is `HttpOnly`; `Secure=true` outside local profile.
- Forwarded headers are enabled (`server.forward-headers-strategy=framework`) for TLS termination proxies (Caddy).

## Quickstart (local)

Prerequisite: Java 21 and Maven 3.9+ installed locally.

1. Start with profile: `SPRING_PROFILES_ACTIVE=local`.
2. Configure one or more OAuth providers in `src/main/resources/application-local.yml` (template provided as comments) or via env vars.
3. Run app:

```bash
mvn spring-boot:run
```

4. Begin auth flow in browser:

```text
http://localhost:8080/oauth2/authorization/{provider}
```

5. From UI, call API with credentials enabled and send `X-XSRF-TOKEN` for POST endpoints.

## Production (Auth0)

1. Configure Auth0 app URLs:
- Application Login URI: `https://auth-api.bondbenbond.com/oauth2/authorization/auth0`
- Allowed Callback URLs: `https://auth-api.bondbenbond.com/login/oauth2/code/auth0`
- Allowed Logout URLs: `https://auth-ui.bondbenbond.com`
- Allowed Web Origins: `https://auth-ui.bondbenbond.com`
- Allowed Origins (CORS): `https://auth-ui.bondbenbond.com`

2. Set environment variables (template: `.env.example`):
- `SPRING_PROFILES_ACTIVE=prod`
- `AUTH0_CLIENT_ID`
- `AUTH0_CLIENT_SECRET`
- `AUTH0_ISSUER_URI` (example: `https://your-tenant.us.auth0.com/`)
- `UI_BASE_URL=https://auth-ui.bondbenbond.com`
- `ALLOWED_ORIGIN=https://auth-ui.bondbenbond.com`
- `DEBUG_TOKENS=false`

3. Deploy API behind TLS termination (Caddy) on `https://auth-api.bondbenbond.com`.

4. Validate flow:
- Open `https://auth-api.bondbenbond.com/oauth2/authorization/auth0`
- Confirm redirect back to UI
- Call `GET /api/whoami` from UI with credentials included
- For POST endpoints, send CSRF header `X-XSRF-TOKEN`

## Docker Deployment

Build and push image:

```bash
docker build -t ghcr.io/bondbenbond/oauth-integration-api:latest .
docker push ghcr.io/bondbenbond/oauth-integration-api:latest
```

Deploy on server (using `docker-compose.yml`):

```bash
export API_TAG=latest
export SPRING_PROFILES_ACTIVE=prod
export AUTH0_CLIENT_ID=...
export AUTH0_CLIENT_SECRET=...
export AUTH0_ISSUER_URI=https://your-tenant.us.auth0.com/
export UI_BASE_URL=https://auth-ui.bondbenbond.com
export ALLOWED_ORIGIN=https://auth-ui.bondbenbond.com
docker compose up -d
```

Example Caddy route:

```caddyfile
auth-api.bondbenbond.com {
  reverse_proxy oauth-integration-api:8080
}
```

## Provider setup notes

For each provider (Okta, Azure AD, Auth0, Keycloak):

- Authorization code flow
- Redirect URI: `{baseUrl}/login/oauth2/code/{registrationId}`
- Scopes: typically `openid profile email`
- Prefer issuer discovery (`issuer-uri`) over hardcoding endpoints

Optional test API target override per provider:

```yaml
app:
  test-resource-uris:
    okta: https://example.okta.com/oauth2/v1/userinfo
```

`POST /api/test/call` will use the configured URI first, then userinfo endpoint from provider metadata.

## Troubleshooting

- `401` on `/api/whoami`: no authenticated session exists yet.
- `403` on POST endpoints: missing/invalid CSRF header `X-XSRF-TOKEN`.
- Redirect URI mismatch: provider app registration redirect URI does not match `{baseUrl}/login/oauth2/code/{registrationId}`.
- No providers listed: no `spring.security.oauth2.client.registration.*` configured.
- Wrong scheme/host behind reverse proxy: verify Caddy forwards `X-Forwarded-*` headers.
