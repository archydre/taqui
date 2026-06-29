import Link from "next/link";
import {
  ApiError,
  getPosts,
  getProducts,
  getUserByUsername,
  type Owner,
  type Post,
  type Product,
} from "@/lib/api";
import { Avatar } from "@/components/avatar";
import { ProductCard } from "@/components/product-card";
import { PostCard } from "@/components/post-card";
import { RevealWhatsapp } from "@/components/reveal-whatsapp";

export default async function PerfilPublico({
  params,
}: {
  params: Promise<{ username: string }>;
}) {
  const { username } = await params;

  let owner: Owner;
  try {
    owner = await getUserByUsername(username);
  } catch (err) {
    const notFound = err instanceof ApiError && err.status === 404;
    return (
      <div className="mx-auto w-full max-w-xl py-10 text-center">
        <p className="text-ink-soft">
          {notFound
            ? `Não encontramos @${username}.`
            : "Não foi possível carregar este perfil."}
        </p>
        <Link href="/" className="mt-3 inline-block text-sm text-action hover:underline">
          Voltar ao feed
        </Link>
      </div>
    );
  }

  let products: Product[] = [];
  let posts: Post[] = [];
  try {
    const [p, f] = await Promise.all([
      getProducts({ owner: username, size: 12 }),
      getPosts({ owner: username, size: 12 }),
    ]);
    products = p.content;
    posts = f.content;
  } catch {
    // mantém listas vazias se algo falhar
  }

  return (
    <div className="mx-auto w-full max-w-2xl">
      <header className="flex items-center gap-4 border-b border-line pb-6">
        <Avatar name={owner.displayName} seed={owner.username} size={72} />
        <div className="min-w-0 flex-1">
          <h1 className="truncate font-display text-2xl font-semibold text-ink">
            {owner.displayName}
          </h1>
          <p className="text-ink-soft">@{owner.username}</p>
        </div>
        {owner.hasWhatsapp ? <RevealWhatsapp username={owner.username} /> : null}
      </header>

      <section className="py-6">
        <h2 className="mb-3 font-display text-lg font-semibold text-ink">
          À venda
        </h2>
        {products.length === 0 ? (
          <p className="text-ink-soft">Nenhum produto à venda.</p>
        ) : (
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
            {products.map((product) => (
              <ProductCard key={product.productId} product={product} />
            ))}
          </div>
        )}
      </section>

      <section className="py-2">
        <h2 className="mb-3 font-display text-lg font-semibold text-ink">
          Publicações
        </h2>
        {posts.length === 0 ? (
          <p className="text-ink-soft">Nenhuma publicação ainda.</p>
        ) : (
          <div className="flex flex-col gap-4">
            {posts.map((post) => (
              <PostCard key={post.postId} post={post} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
