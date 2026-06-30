import Link from "next/link";
import { ApiError, getProductById, type Product } from "@/lib/api";
import { formatPrice } from "@/lib/format";
import { Avatar } from "@/components/avatar";
import { RevealWhatsapp } from "@/components/reveal-whatsapp";
import { ProductOwnerActions } from "@/components/product-owner-actions";
import { BuyButton } from "@/components/buy-button";
import { CommentsSection } from "@/components/comments-section";

export default async function ProdutoPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;

  let product: Product;
  try {
    product = await getProductById(id);
  } catch (err) {
    const notFound = err instanceof ApiError && err.status === 404;
    return (
      <div className="mx-auto w-full max-w-xl py-10 text-center">
        <p className="text-ink-soft">
          {notFound
            ? "Produto não encontrado."
            : "Não foi possível carregar o produto."}
        </p>
        <Link href="/explorar" className="mt-3 inline-block text-sm text-action hover:underline">
          Voltar para Explorar
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto w-full max-w-4xl py-4">
      <div className="grid gap-8 md:grid-cols-2">
      <div className="overflow-hidden rounded-2xl border border-line bg-surface">
        <div className="aspect-square bg-line">
          {product.imageUrl ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={product.imageUrl}
              alt={product.productName}
              className="h-full w-full object-cover"
            />
          ) : (
            <div className="flex h-full w-full items-center justify-center text-ink-soft">
              sem foto
            </div>
          )}
        </div>
      </div>

      <div className="flex flex-col">
        <h1 className="font-display text-2xl font-semibold text-ink">
          {product.productName}
        </h1>
        <p className="mt-2 font-display text-3xl font-bold text-price">
          {formatPrice(product.price)}
        </p>

        <Link
          href={`/u/${product.owner.username}`}
          className="mt-6 flex items-center gap-3 rounded-xl border border-line bg-surface p-3 hover:bg-ink/5"
        >
          <Avatar name={product.owner.displayName} seed={product.owner.username} />
          <div className="min-w-0">
            <p className="truncate text-sm font-medium text-ink">
              {product.owner.displayName}
            </p>
            <p className="truncate text-xs text-ink-soft">
              @{product.owner.username}
            </p>
          </div>
        </Link>

        <div className="mt-6 flex flex-col gap-3">
          <BuyButton
            productId={product.productId}
            ownerUsername={product.owner.username}
          />
          {product.owner.hasWhatsapp ? (
            <RevealWhatsapp username={product.owner.username} />
          ) : null}
          <ProductOwnerActions
            productId={product.productId}
            ownerUsername={product.owner.username}
          />
        </div>
      </div>
      </div>

      <section className="mt-8">
        <h2 className="font-display text-lg font-semibold text-ink">
          Descrição do produto
        </h2>
        {product.productDescription ? (
          <p className="mt-2 whitespace-pre-wrap text-[15px] leading-relaxed text-ink">
            {product.productDescription}
          </p>
        ) : (
          <p className="mt-2 text-sm text-ink-soft">Sem descrição.</p>
        )}
      </section>

      <section className="mt-8">
        <h2 className="font-display text-lg font-semibold text-ink">Comentários</h2>
        <CommentsSection
          target="product"
          targetId={product.productId}
          ownerUsername={product.owner.username}
        />
      </section>
    </div>
  );
}
