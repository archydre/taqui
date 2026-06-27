# Backend — taqui

API REST em Spring Boot. Stack, comandos e banco estão no [CLAUDE.md](./CLAUDE.md).

Com o app rodando, o Swagger UI fica em `/swagger-ui.html`.

## Endpoints

### Auth

Rotas públicas.

| Método | Rota | O que faz |
|--------|------|-----------|
| POST | `/auth/register` | cria um usuário (email, password, username, displayName) |
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
`price` e `imageUrl`. A resposta inclui o `owner` (id, username e displayName) e os timestamps.

### Posts

Todas precisam do header `Authorization: Bearer <jwt>`.

| Método | Rota | O que faz |
|--------|------|-----------|
| POST | `/posts` | cria um post; o dono vem do token |
| GET | `/posts` | lista o feed, paginado e do mais novo pro mais antigo |
| GET | `/posts/{postId}` | retorna um |
| PUT | `/posts/{postId}` | atualiza, só o dono |
| DELETE | `/posts/{postId}` | remove, só o dono |

Há dois tipos, definidos por enviar ou não `productId` no corpo (`content`, `imageUrl`,
`productId`):

- **anúncio** — `productId` aponta pra um produto **seu** (senão 403); a resposta traz o
  produto aninhado e `type: "ANUNCIO"`.
- **comum** — sem `productId`; precisa de `content` ou `imageUrl`, senão 400 (`type: "COMUM"`).

A resposta (`PostResponseDTO`) inclui `owner` (id, username e displayName), o `product`
(null no post comum) e os timestamps.

O `GET /posts` é paginado: aceita `?page` (default 0) e `?size` (default 20) e devolve um
`Page` — os posts vêm em `content` e os metadados (`totalElements`, `totalPages`, `number`,
`first`, `last`...) no mesmo objeto. A ordem é sempre do mais novo pro mais antigo (`createdAt` desc).

Os erros usam o formato ProblemDetail (RFC 7807). Os códigos: 400 quando o corpo é
inválido, 401 sem token ou token inválido, 403 quando você não é o dono, 404 quando o
recurso não existe e 409 no register quando o email ou o username já está cadastrado.

### Uploads

Precisa do header `Authorization: Bearer <jwt>`.

| Método | Rota | O que faz |
|--------|------|-----------|
| POST | `/uploads` | gera uma URL pré-assinada pra subir uma imagem direto no R2 |

O corpo é o `UploadRequestDTO`: `contentType` (só `image/png`, `image/jpeg` ou `image/webp`). A
resposta (`UploadResponseDTO`) traz `uploadUrl` (URL pré-assinada — faça o `PUT` do arquivo nela
com o mesmo `Content-Type`) e `publicUrl` (URL pública final, é ela que vai no `imageUrl` do
produto/post). A `uploadUrl` expira em poucos minutos.

### Users

| Método | Rota | O que faz |
|--------|------|-----------|
| GET | `/users/{username}` | perfil público de um usuário (não precisa de login) |

Modelo **guest browsing**: leitura de vitrine é pública, ação exige login. O `GET
/users/{username}` é aberto e devolve o `UserPublicInfoDTO` (id, username e displayName —
**sem email**); responde 404 se o username não existir. O `username` é validado no register por
`^[a-z0-9._]{3,30}$` e é único (409 se repetir).

## Ainda falta

- Perfil do próprio usuário (`GET /users/me`, com email) e busca (`GET /users?q=`).
