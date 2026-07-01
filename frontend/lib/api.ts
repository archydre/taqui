// No navegador usamos a URL pública (localhost:8080). No servidor (SSR dentro do
// container) localhost é o próprio front, então preferimos API_INTERNAL_URL
// (ex.: http://backend:8080, o nome do serviço no compose).
const API_BASE_URL =
  (typeof window === "undefined" ? process.env.API_INTERNAL_URL : undefined) ??
  process.env.NEXT_PUBLIC_API_BASE_URL ??
  "http://localhost:8080";

export class ApiError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

type RequestOptions = {
  method?: "GET" | "POST" | "PUT" | "DELETE";
  token?: string | null;
  body?: unknown;
  timeoutMs?: number;
};

// Mensagem amigável por status, sem expor caminho nem ID interno.
function statusMessage(status: number): string {
  if (status === 400) return "Requisição inválida.";
  if (status === 401) return "Sua sessão expirou. Entre novamente.";
  if (status === 403) return "Você não tem permissão para isso.";
  if (status === 404) return "Não encontramos o que você procura.";
  if (status === 409) return "Isso conflita com o estado atual.";
  if (status === 422) return "Não foi possível processar os dados enviados.";
  if (status >= 500) return "Erro no servidor. Tente novamente em instantes.";
  return "Algo deu errado. Tente novamente.";
}

const UUID_PATTERN =
  /\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\b/gi;

// Remove UUIDs de qualquer texto que vá para a tela.
function stripIds(message: string): string {
  return message.replace(UUID_PATTERN, "").replace(/\s{2,}/g, " ").trim();
}

// Extrai a mensagem de um corpo ProblemDetail: detail/message direto, ou o
// mapa "errors" das falhas de validação (MethodArgumentNotValidException).
function messageFromProblem(problem: unknown): string | null {
  if (!problem || typeof problem !== "object") return null;
  const p = problem as Record<string, unknown>;
  const direct = p.detail ?? p.message;
  if (typeof direct === "string" && direct.trim()) return direct;
  if (p.errors && typeof p.errors === "object") {
    const msgs = Object.values(p.errors as Record<string, unknown>).filter(
      (v): v is string => typeof v === "string" && v.trim().length > 0,
    );
    if (msgs.length) return msgs.join(" ");
  }
  return null;
}

// Callback disparado quando uma chamada autenticada recebe 401 (token inválido/expirado).
let onUnauthorized: (() => void) | null = null;

export function setUnauthorizedHandler(handler: (() => void) | null): void {
  onUnauthorized = handler;
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = "GET", token, body, timeoutMs } = options;
  const res = await fetch(new URL(path, API_BASE_URL), {
    method,
    cache: "no-store",
    // Sem timeout o fetch espera pra sempre; com ele, um back travado vira erro.
    signal: timeoutMs ? AbortSignal.timeout(timeoutMs) : undefined,
    headers: {
      ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    if (res.status === 401 && token) onUnauthorized?.();
    let detail = statusMessage(res.status);
    try {
      const fromBody = messageFromProblem(await res.json());
      if (fromBody) detail = stripIds(fromBody) || detail;
    } catch {
      // resposta sem corpo JSON: mantém a mensagem amigável
    }
    throw new ApiError(detail, res.status);
  }

  if (res.status === 204) {
    return undefined as T;
  }
  return res.json() as Promise<T>;
}

export type Owner = {
  userId: string;
  username: string;
  displayName: string;
  hasWhatsapp: boolean;
};

export type Product = {
  productId: string;
  productName: string;
  productDescription: string | null;
  price: number;
  imageUrl: string | null;
  thumbnailUrl?: string | null;
  weight: number | null;
  width: number | null;
  height: number | null;
  length: number | null;
  owner: Owner;
  createdAt: string;
  updatedAt: string;
};

export type PostType = "ANUNCIO" | "COMUM";

export type Post = {
  postId: string;
  content: string | null;
  imageUrl: string | null;
  thumbnailUrl?: string | null;
  product: Product | null;
  type: PostType | string;
  owner: Owner;
  createdAt: string;
  updatedAt: string;
};

export type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
};

export type Me = {
  userId: string;
  email: string;
  username: string;
  displayName: string;
  whatsapp: string | null;
  pixKey: string | null;
  postalCode: string | null;
};

export type LoginResponse = {
  token: string;
  tokenType: string;
  expiresInMinutes: number;
};

export type FreightOption = {
  id: number;
  name: string;
  price: string;
  customPrice?: string | null;
  deliveryTime: number | null;
  company: { id: number; name: string; picture: string | null } | null;
};

export type Address = {
  recipientName: string;
  postalCode: string;
  street: string;
  number: string;
  complement?: string;
  district: string;
  city: string;
  state: string;
};

export type OrderStatus =
  | "AGUARDANDO_PAGAMENTO"
  | "PAGO"
  | "ENVIADO"
  | "CANCELADO";

export type Order = {
  orderId: string;
  buyer: Owner;
  product: Product;
  quantity: number;
  unitPrice: number;
  freightService: string;
  freightPrice: number;
  total: number;
  sellerPixKey: string;
  address: Address;
  status: OrderStatus;
  createdAt: string;
  updatedAt: string;
};

function buildQuery(params: Record<string, string | number | undefined>): string {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") search.set(key, String(value));
  }
  const qs = search.toString();
  return qs ? `?${qs}` : "";
}

// ---- Vitrine / feed (públicos) ----

export type ProductQuery = { q?: string; owner?: string; page?: number; size?: number };

export function getProducts(query: ProductQuery = {}): Promise<Page<Product>> {
  return request(`/products${buildQuery(query)}`);
}

export function getProductById(productId: string): Promise<Product> {
  return request(`/products/${productId}`);
}

export type PostQuery = { owner?: string; page?: number; size?: number };

export function getPosts(query: PostQuery = {}): Promise<Page<Post>> {
  return request(`/posts${buildQuery(query)}`);
}

// ---- Auth ----

export function login(email: string, password: string): Promise<LoginResponse> {
  return request("/auth/login", { method: "POST", body: { email, password } });
}

export type RegisterInput = {
  email: string;
  password: string;
  username: string;
  displayName: string;
};

export function register(input: RegisterInput): Promise<unknown> {
  return request("/auth/register", { method: "POST", body: input });
}

export function verifyEmail(token: string): Promise<void> {
  return request(`/auth/verify?token=${encodeURIComponent(token)}`, {
    method: "POST",
    timeoutMs: 10_000,
  });
}

// ---- Usuário ----

export function getMe(token: string): Promise<Me> {
  return request("/users/me", { token });
}

export type UpdateMeInput = {
  whatsapp?: string;
  displayName?: string;
  pixKey?: string;
  postalCode?: string;
};

export function updateMe(token: string, input: UpdateMeInput): Promise<Me> {
  return request("/users/me", { method: "PUT", token, body: input });
}

export function getUserByUsername(username: string): Promise<Owner> {
  return request(`/users/${username}`);
}

export function revealWhatsapp(
  token: string,
  username: string,
): Promise<{ username: string; whatsapp: string }> {
  return request(`/users/${username}/whatsapp`, { token });
}

// ---- Upload de imagem (multipart -> back -> R2) ----

export type UploadResult = { imageUrl: string; thumbnailUrl: string };

export async function uploadImage(token: string, file: File): Promise<UploadResult> {
  const form = new FormData();
  form.append("file", file);
  // Sem Content-Type manual: o browser define o boundary do multipart.
  const res = await fetch(new URL("/uploads", API_BASE_URL), {
    method: "POST",
    cache: "no-store",
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    body: form,
  });
  if (!res.ok) {
    if (res.status === 401 && token) onUnauthorized?.();
    let detail = statusMessage(res.status);
    try {
      const fromBody = messageFromProblem(await res.json());
      if (fromBody) detail = stripIds(fromBody) || detail;
    } catch {
      // resposta sem corpo JSON: mantém a mensagem amigável
    }
    throw new ApiError(detail, res.status);
  }
  return res.json() as Promise<UploadResult>;
}

// ---- Criar produto / post ----

export type CreateProductInput = {
  productName: string;
  productDescription?: string;
  price: number;
  imageUrl?: string;
  thumbnailUrl?: string;
  weight?: number;
  width?: number;
  height?: number;
  length?: number;
};

export function createProduct(token: string, input: CreateProductInput): Promise<Product> {
  return request("/products", { method: "POST", token, body: input });
}

export type CreatePostInput = {
  content?: string;
  imageUrl?: string;
  thumbnailUrl?: string;
  productId?: string;
};

export function createPost(token: string, input: CreatePostInput): Promise<Post> {
  return request("/posts", { method: "POST", token, body: input });
}

export function updatePost(token: string, postId: string, input: CreatePostInput): Promise<Post> {
  return request(`/posts/${postId}`, { method: "PUT", token, body: input });
}

export function deletePost(token: string, postId: string): Promise<void> {
  return request(`/posts/${postId}`, { method: "DELETE", token });
}

// ---- Comentários (post e produto) ----

export type Comment = {
  commentId: string;
  author: Owner;
  content: string;
  createdAt: string;
};

export function getPostComments(postId: string, page = 0, size = 50): Promise<Page<Comment>> {
  return request(`/posts/${postId}/comments${buildQuery({ page, size })}`);
}

export function createPostComment(
  token: string,
  postId: string,
  content: string,
): Promise<Comment> {
  return request(`/posts/${postId}/comments`, { method: "POST", token, body: { content } });
}

export function getProductComments(
  productId: string,
  page = 0,
  size = 50,
): Promise<Page<Comment>> {
  return request(`/products/${productId}/comments${buildQuery({ page, size })}`);
}

export function createProductComment(
  token: string,
  productId: string,
  content: string,
): Promise<Comment> {
  return request(`/products/${productId}/comments`, {
    method: "POST",
    token,
    body: { content },
  });
}

export function deleteComment(token: string, commentId: string): Promise<void> {
  return request(`/comments/${commentId}`, { method: "DELETE", token });
}

export function updateProduct(
  token: string,
  productId: string,
  input: CreateProductInput,
): Promise<Product> {
  return request(`/products/${productId}`, { method: "PUT", token, body: input });
}

export function deleteProduct(token: string, productId: string): Promise<void> {
  return request(`/products/${productId}`, { method: "DELETE", token });
}

// ---- Frete ----

export function quoteFreightForProduct(
  token: string,
  productId: string,
  toPostalCode: string,
  quantity: number,
): Promise<FreightOption[]> {
  return request(`/freight/quote/product/${productId}`, {
    method: "POST",
    token,
    body: { toPostalCode, quantity },
  });
}

// ---- Pedidos ----

export type CreateOrderInput = {
  productId: string;
  quantity: number;
  freightService: string;
  freightPrice: number;
  address: Address;
};

export function createOrder(token: string, input: CreateOrderInput): Promise<Order> {
  return request("/orders", { method: "POST", token, body: input });
}

export function getMyOrders(token: string, page = 0, size = 20): Promise<Page<Order>> {
  return request(`/orders${buildQuery({ page, size })}`, { token });
}

export function getOrderById(token: string, orderId: string): Promise<Order> {
  return request(`/orders/${orderId}`, { token });
}

export function productImage(product: Product): string | null {
  return product.thumbnailUrl ?? product.imageUrl;
}
