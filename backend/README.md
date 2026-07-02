# Backend — taqui

API REST em Spring Boot. Stack, comandos e banco estão no [CLAUDE.md](./CLAUDE.md).

Com o app rodando, o Swagger UI fica em `/swagger-ui.html`.

## Endpoints

### Auth

Rotas públicas.

| Método | Rota | O que faz |
|--------|------|-----------|
| POST | `/auth/register` | cria um usuário (email, password, username, displayName) e dispara o email de verificação |
| POST | `/auth/login` | valida as credenciais e devolve um JWT |
| POST | `/auth/verify?token=` | confirma o email pelo token do link (marca `emailVerified`); token inválido/expirado → **400** |

No registro o back gera um token de verificação (válido por 24h) e publica na fila `email.verify.request`;
o worker Python (`services/Emailverify.py`) monta e envia o email. O link do email aponta pro front
(`/verify?token=`), que então chama `POST /auth/verify`. O envio é best-effort: se o email falhar, a conta
ainda é criada (o cadastro responde **201**). O `emailVerified` do usuário aparece em `GET /users/me`.

### Products

Os GET são públicos (vitrine, sem login). Criar, atualizar e apagar precisam do header
`Authorization: Bearer <jwt>`.

| Método | Rota | O que faz |
|--------|------|-----------|
| POST | `/products` | cria um produto; o dono vem do token (exige WhatsApp e chave Pix cadastrados) |
| GET | `/products` | lista a vitrine, paginada e do mais novo pro mais antigo; `?q=termo` busca, `?owner={username}` filtra por vendedor |
| GET | `/products/{productId}` | retorna um |
| PUT | `/products/{productId}` | atualiza, só o dono |
| DELETE | `/products/{productId}` | remove, só o dono |

O corpo de criar e atualizar é o `ProductRequestDTO`: `productName`, `productDescription`,
`price`, `imageUrl`, `thumbnailUrl` e as dimensões pro frete (`weight` em kg, `width`/`height`/`length` em cm — todas opcionais). O `imageUrl` é a imagem original e o `thumbnailUrl` o thumb 400×400 — ambos vêm do `POST /uploads` (o front manda os dois; use o thumb na vitrine e o original no detalhe). A resposta inclui o `owner` (id, username, displayName e `hasWhatsapp`) e os
timestamps. **Pra criar produto o vendedor precisa ter WhatsApp e chave Pix cadastrados** — sem
WhatsApp ou sem Pix, o `POST` responde **422** ("Cadastre seu WhatsApp para vender" / "Cadastre sua
chave Pix para vender"). Ver a seção Users.

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
`thumbnailUrl`, `productId`):

- **anúncio** — `productId` aponta pra um produto **seu** (senão 403); a resposta traz o
  produto aninhado e `type: "ANUNCIO"`.
- **comum** — sem `productId`; precisa de `content` ou `imageUrl`, senão 400 (`type: "COMUM"`).

A resposta (`PostResponseDTO`) inclui `owner` (id, username e displayName), o `product`
(null no post comum), o `imageUrl`/`thumbnailUrl` (do post comum) e os timestamps.

O `GET /posts` é paginado: aceita `?page` (default 0) e `?size` (default 20) e devolve um
`Page` — os posts vêm em `content` e os metadados (`totalElements`, `totalPages`, `number`,
`first`, `last`...) no mesmo objeto. A ordem é sempre do mais novo pro mais antigo (`createdAt` desc).
Com `?owner={username}` lista só os posts daquele vendedor; se o `username` não existir, responde 404.

Os erros usam o formato ProblemDetail (RFC 7807). Os códigos: 400 quando o corpo é
inválido, 401 sem token ou token inválido, 403 quando você não é o dono, 404 quando o
recurso não existe e 409 no register quando o email ou o username já está cadastrado.

### Comentários (Comments)

Comentários de um post ou de um produto. Os GET são públicos (vitrine, sem login); comentar e apagar
precisam do header `Authorization: Bearer <jwt>`.

| Método | Rota | O que faz |
|--------|------|-----------|
| GET | `/posts/{postId}/comments` | lista os comentários de um post, paginado |
| POST | `/posts/{postId}/comments` | comenta num post (o autor vem do token) |
| GET | `/products/{productId}/comments` | lista os comentários de um produto, paginado |
| POST | `/products/{productId}/comments` | comenta num produto (o autor vem do token) |
| DELETE | `/comments/{commentId}` | apaga um comentário |

O corpo de criar é o `CommentRequestDTO`: só `content` (obrigatório, até 500 caracteres). A resposta
(`CommentResponseDTO`) traz `commentId`, o `author` (`UserPublicInfoDTO`), o `content` e o `createdAt`.
Internamente é uma entidade `Comment` única com FKs anuláveis pra post e produto (exatamente um
preenchido).

Os GET são paginados (`?page`/`?size`, default 0/20) e devolvem um `Page` na ordem do mais novo pro
mais antigo (`createdAt` desc); respondem **404** se o post/produto não existir. Apagar exige ser o
**autor do comentário** ou o **dono** do post/produto (senão **403**) e responde **404** se o
comentário não existir.

### Uploads

Precisa do header `Authorization: Bearer <jwt>`.

| Método | Rota | O que faz |
|--------|------|-----------|
| POST | `/uploads` | sobe uma imagem (proxy): guarda o original no R2 e gera um thumbnail 400×400 |

Envie como **`multipart/form-data`** com o campo **`file`** (a imagem). Os tipos aceitos são
`image/png`, `image/jpeg` e `image/webp`, e o tamanho máximo é **5 MB** por arquivo
(`spring.servlet.multipart.max-file-size`). O backend faz o **proxy** do upload — recebe os bytes,
gera o thumbnail e sobe os dois no R2 (não há mais URL pré-assinada).

O thumbnail (400×400, center-crop, JPEG) é gerado pelo **worker Python** `services/Padrao-Img.py`
via RabbitMQ RPC (fila `image.resize.request`), igual ao frete — sem o worker (ou sem
`CLOUDAMQP_URL`) o endpoint responde **503**.

A resposta (`UploadResponseDTO`) traz `imageUrl` (URL pública do original) e `thumbnailUrl` (URL
pública do thumbnail): use o original no detalhe do produto/post e o thumbnail na vitrine/feed.
Erros (ProblemDetail): **400** arquivo vazio ou content-type inválido, **401** sem token e **503**
se o worker de imagem estiver indisponível.

### Users

| Método | Rota | O que faz |
|--------|------|-----------|
| GET | `/users` | busca pública por username ou displayName, `?q=termo`, paginada |
| GET | `/users/{username}` | perfil público de um usuário (não precisa de login) |
| GET | `/users/{username}/whatsapp` | revela o WhatsApp do vendedor (precisa de login) |
| GET | `/users/me` | perfil do usuário logado, com email, whatsapp, chave Pix e CEP (precisa de login) |
| PUT | `/users/me` | edita o próprio perfil: whatsapp, displayName, chave Pix e/ou CEP de origem (precisa de login) |

Modelo **guest browsing**: leitura de vitrine é pública, ação exige login. O `GET
/users/{username}` é aberto e devolve o `UserPublicInfoDTO` (id, username, displayName e
`hasWhatsapp` — **sem email e sem o número**); responde 404 se o username não existir. O `GET
/users?q=termo` busca por `username` ou `displayName` (parcial, ignora maiúsculas) e devolve um
`Page` de `UserPublicInfoDTO` (`?page`/`?size`); exige ao menos 2 caracteres no `q`, senão vem
vazio. O `GET /users/me` exige token e devolve o `UserResponseDTO` (com email e whatsapp) do dono
do token. O `username` é validado no register por `^[a-z0-9._]{3,30}$`, é único (409 se repetir) e
alguns nomes são reservados (ex.: `me`, `admin`).

**WhatsApp / contato e Pix:** a venda acontece **no site** (ver a seção Pedidos) — o WhatsApp do
vendedor agora é só pra **tirar dúvidas**, mas **segue obrigatório pra vender**. O número fica no
campo `whatsapp` do user (opcional no cadastro; só dígitos com DDI, ex. `5584999998888`); a chave Pix
fica no campo `pixKey`. Regras:

- **Não são públicos.** O `UserPublicInfoDTO` expõe só o boolean `hasWhatsapp` (true/false), nunca o
  número nem o Pix — pro front decidir se mostra o botão "Chamar no WhatsApp". O Pix do vendedor só é
  revelado ao comprador dentro do pedido (`sellerPixKey`, ver Pedidos).
- **Obrigatórios pra vender.** `POST /products` sem `whatsapp` **ou** sem `pixKey` cadastrado → **422**.
- **Cadastrar/trocar:** `PUT /users/me` com o `UpdateMeRequestDTO` (`whatsapp`, `displayName` e/ou
  `pixKey`, todos opcionais — manda só o que quer mudar; `whatsapp` fora do padrão de dígitos → 400).
  O `whatsapp` e o `pixKey` são **únicos**: se já estiverem em uso por outra conta → **409** ("Este
  número já está sendo utilizado" / "Esta chave Pix já está sendo utilizada"). Devolve o
  `UserResponseDTO` atualizado (que pro dono inclui `whatsapp` e `pixKey`).
- **Revelar o número:** `GET /users/{username}/whatsapp` (autenticado — guest leva 401) devolve
  `{ username, whatsapp }`; responde 404 se o usuário não existe ou não tem número cadastrado.

### Frete (RabbitMQ)

Integração com os **workers Python** em `services/` via **RabbitMQ RPC** (request/reply com
`reply_to` + `correlation_id`). O broker é o CloudAMQP — configure a env `CLOUDAMQP_URL`
(usada em `spring.rabbitmq.addresses`). O backend é o **cliente**: publica na fila do worker e
espera a resposta; o worker (`services/Frete.py`) é quem consome e responde. Sem o worker rodando
(ou sem `CLOUDAMQP_URL`), o endpoint responde **503**.

Precisa do header `Authorization: Bearer <jwt>`.

| Método | Rota | O que faz |
|--------|------|-----------|
| POST | `/freight/quote` | cota o frete de uma entrega nas transportadoras (Melhor Envio) |
| POST | `/freight/quote/product/{productId}` | cota o frete de um produto usando as dimensões dele + o CEP do vendedor (o comprador só passa o CEP de destino) |

O corpo é o `FreightQuoteRequestDTO`: `fromPostalCode` e `toPostalCode` (CEP do remetente e do
destinatário, **8 dígitos sem traço**) e `items` (lista, ao menos 1). Cada item: `width`, `height`,
`length` (cm) e `weight` (kg) são obrigatórios; `insuranceValue` (R$, default 0) e `quantity`
(default 1) são opcionais.

A resposta é uma **lista** de `FreightOptionDTO`, já ordenada da mais barata pra mais cara, cada uma
com `id`, `name` (serviço), `price`, `customPrice`, `deliveryTime` (dias) e `company` (`id`, `name`,
`picture`). Erro do serviço de frete (worker fora do ar, timeout ou erro da API do Melhor Envio)
vira **503** no formato ProblemDetail.

A fila do worker é `freight.calculate.request` (durável, declarada pelo próprio worker). O timeout de
resposta do backend é 20s (`spring.rabbitmq.template.reply-timeout`), maior que o timeout que o
worker usa na API do Melhor Envio.

> **Dois modos:** o `/freight/quote` é stateless (quem chama passa CEPs + dimensões no corpo). Já o
> `/freight/quote/product/{productId}` lê as dimensões do produto e o CEP de origem do vendedor do
> banco — o comprador só manda o CEP de destino (e a quantidade), e o CEP do vendedor não vaza.
> Corpo: `ProductFreightQuoteRequestDTO` (`toPostalCode`, `quantity` opcional). Responde **422** se o
> produto está sem dimensões ou o vendedor sem CEP cadastrado.

### Pedidos (Orders)

A venda acontece no site: o comprador escolhe um produto, cota o frete (ver Frete), monta o pedido e
paga via **Pix manual** — o app mostra a chave Pix do vendedor, o pagamento é feito fora do app e o
vendedor confirma o recebimento na mão. Compra **unitária** (um produto por pedido, com quantidade),
sem carrinho. Todas as rotas precisam do header `Authorization: Bearer <jwt>`.

| Método | Rota | O que faz |
|--------|------|-----------|
| POST | `/orders` | comprador cria um pedido (produto, quantidade, frete escolhido e endereço) |
| GET | `/orders` | meus pedidos como comprador, paginado |
| GET | `/orders/received` | pedidos recebidos como vendedor, paginado |
| GET | `/orders/{orderId}` | um pedido (só o comprador ou o vendedor dele) |
| POST | `/orders/{orderId}/confirm-payment` | o vendedor confirma o Pix recebido |
| POST | `/orders/{orderId}/ship` | o vendedor marca como enviado (só se já `PAGO`) |
| POST | `/orders/{orderId}/cancel` | comprador ou vendedor cancela (enquanto não enviado) |

O corpo de criar é o `OrderRequestDTO`: `productId`, `quantity` (≥ 1), `freightService` (nome da
opção de frete escolhida no `/freight/quote`), `freightPrice` (preço dela) e `address`
(`recipientName`, `postalCode` 8 dígitos, `street`, `number`, `complement` opcional, `district`,
`city`, `state` com 2 letras). O servidor **confia** na opção de frete enviada (não recalcula).

Na criação o pedido congela **snapshots** do momento: `unitPrice` (preço do produto), `freightPrice`,
`total` (`unitPrice × quantity + freightPrice`) e `sellerPixKey` (a chave Pix do vendedor) — mudar o
produto ou o Pix depois não altera pedidos já feitos. O pedido nasce `AGUARDANDO_PAGAMENTO`; o
comprador paga no Pix mostrado e o vendedor chama o `confirm-payment` pra marcar `PAGO`. Status:
`AGUARDANDO_PAGAMENTO → PAGO → ENVIADO`, mais `CANCELADO`.

A resposta (`OrderResponseDTO`) traz o `buyer` (público), o `product` aninhado (com o vendedor), os
snapshots, o `sellerPixKey`, o `address`, o `status` e os timestamps. Erros (ProblemDetail): **404**
produto ou pedido inexistente, **403** se você não é participante do pedido (no GET) ou não é o
vendedor (no confirm), **409** ao confirmar um pedido que não está `AGUARDANDO_PAGAMENTO` ou ao comprar o próprio produto, **422** se
o vendedor não tem Pix cadastrado, **400** na validação do corpo.

### Endereços salvos (Addresses)

Endereços de entrega que o usuário salva para **reusar no checkout** em vez de digitar tudo de novo.
Um usuário tem N endereços (tabela `user_addresses`). Todas as rotas precisam do header
`Authorization: Bearer <jwt>` e operam **só** sobre os endereços do dono autenticado.

| Método | Rota | O que faz |
|--------|------|-----------|
| GET | `/addresses` | lista meus endereços salvos (mais recentes primeiro) |
| POST | `/addresses` | salva um endereço novo |
| DELETE | `/addresses/{addressId}` | apaga um endereço meu |

O corpo de criar é o mesmo `AddressDTO` do pedido (`recipientName`, `postalCode` 8 dígitos, `street`,
`number`, `complement` opcional, `district`, `city`, `state` com 2 letras). A resposta
(`SavedAddressDTO`) é esse endereço com um `id`.

O endereço salvo é só a **fonte** para pré-preencher o checkout: ao criar o pedido o `address`
continua sendo **copiado (snapshot)** para dentro do pedido, então editar ou apagar um endereço
salvo depois **não** mexe em pedidos já feitos. Erros (ProblemDetail): **404** endereço inexistente,
**403** ao apagar um endereço que não é seu, **400** na validação do corpo.

### Carrinho (Cart)

Itens que o usuário salva para comprar depois (tabela `cart_items`, 1 linha por produto). Todas as
rotas precisam do header `Authorization: Bearer <jwt>` e operam **só** sobre o carrinho do dono
autenticado. Ao contrário do pedido, o carrinho **não** guarda snapshot de preço/frete/endereço: o
checkout continua sendo por produto/vendedor (rota de pedidos), e é lá que preço e frete são fixados.

| Método | Rota | O que faz |
|--------|------|-----------|
| GET | `/cart` | lista meus itens (com o produto embutido), mais recentes primeiro |
| POST | `/cart/items` | adiciona um produto; se já está no carrinho, **soma** a quantidade |
| PATCH | `/cart/items/{productId}` | define a quantidade do item (`quantity >= 1`) |
| DELETE | `/cart/items/{productId}` | remove o item do carrinho |

O corpo de adicionar é `{ "productId": UUID, "quantity": >=1 }`; o de atualizar é `{ "quantity": >=1 }`.
A resposta (`CartItemResponseDTO`) traz `id`, o `product` completo e a `quantity`. A unicidade
`(buyer, product)` garante 1 linha por produto — por isso a rota é chaveada por `productId`. Erros
(ProblemDetail): **404** produto inexistente ou item fora do carrinho, **409** ao adicionar o próprio
produto, **400** na validação do corpo.

### Notificações (Notifications)

Notificam o usuário dos eventos de pedido, **in-app** (contador de não-lidas) e por **email**. Cada
transição do pedido publica um evento de domínio; um listener `@Async` + `@TransactionalEventListener(AFTER_COMMIT)`
grava a notificação (só **depois** do commit do pedido, nunca em cima de um rollback, e sem travar a
request) e publica o email numa fila do RabbitMQ. Nunca notifica o **autor** da ação, só a contraparte.
Todas as rotas precisam do header `Authorization: Bearer <jwt>` e são escopadas ao usuário do token.

| Método | Rota | O que faz |
|--------|------|-----------|
| GET | `/notifications` | minhas notificações, paginadas, das mais novas pras mais antigas |
| GET | `/notifications/unread-count` | quantas não lidas (`{ "count": N }`) — query quente do sino |
| PUT | `/notifications/{id}/read` | marca uma como lida (só a própria; senão **404**) |
| PUT | `/notifications/read-all` | marca todas as minhas como lidas |

Eventos e destinatário: **NEW_ORDER** (comprador cria → vendedor), **PAYMENT_CONFIRMED** (vendedor
confirma o Pix → comprador), **ORDER_SHIPPED** (vendedor envia → comprador), **ORDER_CANCELLED**
(um cancela → a contraparte). A `NotificationResponseDTO` traz `id`, `type`, `relatedEntityType`
(`ORDER`) + `relatedEntityId` (pro front linkar), `message` (texto já montado), `read` e `createdAt`.
O email reusa o worker Python (`services/Emailverify.py`, fila `notification.email.request`,
fire-and-forget) — sem `GMAIL_USER`/`GMAIL_APP_PASSWORD` no worker, o in-app funciona e o email é
só descartado. As notificações são best-effort: uma falha ao notificar nunca derruba o pedido.
