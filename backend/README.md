# Backend — taqui

API REST em Spring Boot. Stack, comandos e banco estão no [CLAUDE.md](./CLAUDE.md).

Com o app rodando, o Swagger UI fica em `/swagger-ui.html`.

## Endpoints

### Auth

Rotas públicas.

| Método | Rota | O que faz |
|--------|------|-----------|
| POST | `/auth/register` | cria um usuário (email, password, username) |
| POST | `/auth/login` | valida as credenciais e devolve um JWT |

### Products

Todas precisam do header `Authorization: Bearer <jwt>`.

| Método | Rota | O que faz |
|--------|------|-----------|
| POST | `/products` | cria um produto; o dono vem do token |
| GET | `/products` | lista todos |
| GET | `/products/{productId}` | retorna um |
| PUT | `/products/{productId}` | atualiza, só o dono |
| DELETE | `/products/{productId}` | remove, só o dono |

O corpo de criar e atualizar é o `ProductRequestDTO`: `productName`, `productDescription`,
`price` e `imageUrl`. A resposta inclui o `owner` (só id e username) e os timestamps.

Os erros usam o formato ProblemDetail (RFC 7807). Os códigos: 400 quando o corpo é
inválido, 401 sem token ou token inválido, 403 quando você não é o dono, 404 quando o
recurso não existe e 409 no register quando o email já está cadastrado.

## Ainda falta

- CRUD de perfil (`/users/me`, `/users/{id}`).
- Módulo de posts pro feed.
