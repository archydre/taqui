# Backend — taqui

API REST (Spring Boot) consumida pelo frontend. Stack, comandos e banco em [`CLAUDE.md`](./CLAUDE.md).

Documentação interativa (gerada): **Swagger UI** em `/swagger-ui.html` quando o app está rodando.

## Endpoints

> Atualizado conforme os endpoints são criados.

### Auth (`/auth`) — público

| Método | Rota | Descrição | Corpo (req) | Resposta |
|--------|------|-----------|-------------|----------|
| `POST` | `/auth/register` | Cadastro de usuário | `RegisterRequestDTO` (email, password, username) | `201` `UserResponseDTO` (sem password) · `400` inválido · `409` email já existe |
| `POST` | `/auth/login` | Login → emite JWT | `LoginRequestDTO` (email, password) | `200` `LoginResponseDTO` (token, tokenType, expiresInMinutes) · `401` credenciais inválidas |

### Products (`/products`) — exige `Authorization: Bearer <jwt>`

| Método | Rota | Descrição | Corpo (req) | Resposta |
|--------|------|-----------|-------------|----------|
| `POST` | `/products` | Cria produto (dono = usuário do token) | `ProductRequestDTO` | `201` `ProductResponseDTO` · `400` inválido · `401` |
| `GET` | `/products` | Lista todos os produtos | — | `200` `ProductResponseDTO[]` · `401` |
| `GET` | `/products/{productId}` | Detalha um produto | — | `200` `ProductResponseDTO` · `404` · `401` |
| `PUT` | `/products/{productId}` | Atualiza (só o dono) | `ProductRequestDTO` | `200` `ProductResponseDTO` · `400` · `403` não é o dono · `404` · `401` |
| `DELETE` | `/products/{productId}` | Remove (só o dono) | — | `204` · `403` não é o dono · `404` · `401` |

**`ProductRequestDTO`**: `productName` (obrigatório, ≤100), `productDescription` (≤500), `price` (obrigatório, > 0), `imageUrl`.
**`ProductResponseDTO`**: `productId`, `productName`, `productDescription`, `price`, `imageUrl`, `owner` (`userId`, `username`), `createdAt`, `updatedAt`.

Erros seguem o padrão **RFC 7807** (`ProblemDetail`).

### Planejados

- `GET /users/me`, `GET /users/{id}`, `PUT /users/me`, `DELETE /users/me` — perfil (exigem autenticação).
- Módulo **Post** (feed) — mesmo molde de Product.
