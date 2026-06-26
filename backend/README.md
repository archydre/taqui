# Backend — taqui

API REST (Spring Boot) consumida pelo frontend. Stack, comandos e banco em [`CLAUDE.md`](./CLAUDE.md).

Documentação interativa (gerada): **Swagger UI** em `/swagger-ui.html` quando o app está rodando.

## Endpoints

> Atualizado conforme os endpoints são criados.

| Método | Rota | Descrição | Auth | Corpo (req) | Resposta |
|--------|------|-----------|------|-------------|----------|
| `POST` | `/auth/register` | Cadastro de usuário | público | `UserLoginDTO` (email, password, username) | `201` `UserResponseDTO` (sem password) · `400` se inválido · `409` se email já existe |

Erros seguem o padrão **RFC 7807** (`ProblemDetail`).

### Planejados

- `POST /auth/login` — login → token/sessão.
- `GET /users/me`, `GET /users/{id}`, `PUT /users/me`, `DELETE /users/me` — perfil (exigem autenticação).
