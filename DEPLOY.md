# Deploy do taqui

O projeto tem 3 partes que rodam em hosts diferentes:

| Parte | O que é | Onde hospedar |
|-------|---------|---------------|
| `frontend/` | Next.js | **Vercel** |
| `backend/` | Spring Boot (Java) | Railway / Render / Fly.io / VPS (Vercel **não** roda) |
| `services/` | Workers Python (frete, imagem) | Railway / Render / VPS + CloudAMQP |

> Vercel hospeda **só o frontend**. Backend e workers precisam de outro host.

---

## 1. Frontend na Vercel

1. **Importar o repositório** na Vercel.
2. **Root Directory = `frontend`** (é um monorepo — Project Settings → Root Directory).
3. Framework: Next.js (detectado automático). Package manager: bun (`bun.lock`).
4. **Variável de ambiente:**

   | Nome | Valor |
   |------|-------|
   | `NEXT_PUBLIC_API_BASE_URL` | URL pública do backend (ex.: `https://api.seudominio.com`) |

   - **Não** defina `API_INTERNAL_URL` na Vercel (é só para o Docker; sem ela, o SSR usa a `NEXT_PUBLIC_API_BASE_URL`).
5. Deploy. O `next.config.ts` já desliga o `output: standalone` na Vercel (`VERCEL=1`).

Build local de validação (o que a Vercel roda):

```bash
cd frontend
bun install
bun run build
```

---

## 2. Backend (Spring Boot) em outro host

Suba o `backend/` (jar ou container) num host que rode Java 21. Variáveis de ambiente:

| Nome | Para quê |
|------|----------|
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | Postgres gerenciado |
| `APP_JWT_SECRET` | segredo do JWT (HS256) |
| `APP_JWT_EXPIRATION_MINUTES` | expiração do token (ex.: 60) |
| `APP_CORS_ALLOWED_ORIGINS` | **incluir o domínio da Vercel** (ex.: `https://taqui.vercel.app`) |
| `CLOUDAMQP_URL` | RabbitMQ (CloudAMQP) — frete e imagem |
| credenciais do **R2** | storage de imagens (bucket, chaves, endpoint) |

Pontos de atenção:
- **CORS:** sem o domínio da Vercel em `APP_CORS_ALLOWED_ORIGINS`, o navegador bloqueia as chamadas.
- **HTTPS:** o front em HTTPS (Vercel) não pode chamar um back em HTTP (mixed content) — o backend precisa de HTTPS.
- Sem `CLOUDAMQP_URL`/worker, frete e upload de imagem respondem **503** (resto funciona).

---

## 3. Workers Python (`services/`)

`Frete.py` e `Padrao-Img.py` consomem filas do **mesmo** RabbitMQ (CloudAMQP) que o backend. Suba-os num host que rode Python, com:

- `CLOUDAMQP_URL` (mesmo broker do backend);
- credenciais do **R2** (o worker de imagem sobe os arquivos).

Sem eles no ar, frete e imagem ficam indisponíveis (503), mas o resto do app funciona.

---

## Resumo do fluxo

```
Navegador → Front (Vercel) → Backend (host Java, HTTPS, CORS liberado p/ Vercel)
                                     ├── Postgres
                                     ├── R2 (imagens)
                                     └── RabbitMQ (CloudAMQP) → Workers Python (frete, imagem)
```
