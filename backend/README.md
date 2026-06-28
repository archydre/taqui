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

Os GET são públicos (vitrine, sem login). Criar, atualizar e apagar precisam do header
`Authorization: Bearer <jwt>`.

| Método | Rota | O que faz |
|--------|------|-----------|
| POST | `/products` | cria um produto; o dono vem do token (exige WhatsApp cadastrado) |
| GET | `/products` | lista a vitrine, paginada e do mais novo pro mais antigo; `?q=termo` busca, `?owner={username}` filtra por vendedor |
| GET | `/products/{productId}` | retorna um |
| PUT | `/products/{productId}` | atualiza, só o dono |
| DELETE | `/products/{productId}` | remove, só o dono |

O corpo de criar e atualizar é o `ProductRequestDTO`: `productName`, `productDescription`,
`price` e `imageUrl`. A resposta inclui o `owner` (id, username, displayName e `hasWhatsapp`) e os
timestamps. **Pra criar produto o vendedor precisa ter WhatsApp cadastrado** — sem ele, o `POST`
responde **422** ("Cadastre seu WhatsApp para vender"). Ver a seção Users.

O `GET /products` é paginado (`?page`/`?size`, default 0/20) e devolve um `Page` (`content` +
metadados), na ordem `createdAt` desc. Com `?q=termo` busca produtos por nome ou descrição (parcial,
ignora maiúsculas; exige ao menos 2 caracteres, senão vem vazio). Com `?owner={username}` lista só os
produtos daquele vendedor; se o `username` não existir, responde 404. O `q` tem precedência sobre o
`owner`.

### Posts

Os GET são públicos (vitrine, sem login). Criar, atualizar e apagar precisam do header
`Authorization: Bearer <jwt>`.

| Método | Rota | O que faz |
|--------|------|-----------|
| POST | `/posts` | cria um post; o dono vem do token |
| GET | `/posts` | lista o feed, paginado e do mais novo pro mais antigo; `?owner={username}` filtra por vendedor |
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
Com `?owner={username}` lista só os posts daquele vendedor; se o `username` não existir, responde 404.

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
| GET | `/users` | busca pública por username ou displayName, `?q=termo`, paginada |
| GET | `/users/{username}` | perfil público de um usuário (não precisa de login) |
| GET | `/users/{username}/whatsapp` | revela o WhatsApp do vendedor (precisa de login) |
| GET | `/users/me` | perfil do usuário logado, com email e whatsapp (precisa de login) |
| PUT | `/users/me` | edita o próprio perfil: whatsapp e/ou displayName (precisa de login) |

Modelo **guest browsing**: leitura de vitrine é pública, ação exige login. O `GET
/users/{username}` é aberto e devolve o `UserPublicInfoDTO` (id, username, displayName e
`hasWhatsapp` — **sem email e sem o número**); responde 404 se o username não existir. O `GET
/users?q=termo` busca por `username` ou `displayName` (parcial, ignora maiúsculas) e devolve um
`Page` de `UserPublicInfoDTO` (`?page`/`?size`); exige ao menos 2 caracteres no `q`, senão vem
vazio. O `GET /users/me` exige token e devolve o `UserResponseDTO` (com email e whatsapp) do dono
do token. O `username` é validado no register por `^[a-z0-9._]{3,30}$`, é único (409 se repetir) e
alguns nomes são reservados (ex.: `me`, `admin`).

**WhatsApp / contato:** a compra acontece no WhatsApp do vendedor — o app **não tem checkout**. O
número fica no campo `whatsapp` do user (opcional no cadastro; só dígitos com DDI, ex.
`5584999998888`). Regras:

- **Não é público.** O `UserPublicInfoDTO` expõe só o boolean `hasWhatsapp` (true/false), nunca o
  número — pro front decidir se mostra o botão "Chamar no WhatsApp".
- **Obrigatório pra vender.** `POST /products` sem `whatsapp` cadastrado → **422**.
- **Cadastrar/trocar:** `PUT /users/me` com o `UpdateMeRequestDTO` (`whatsapp` e/ou `displayName`,
  ambos opcionais — manda só o que quer mudar; `whatsapp` fora do padrão de dígitos → 400). Devolve
  o `UserResponseDTO` atualizado.
- **Revelar o número:** `GET /users/{username}/whatsapp` (autenticado — guest leva 401) devolve
  `{ username, whatsapp }`; responde 404 se o usuário não existe ou não tem número cadastrado.
