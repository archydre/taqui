import Link from "next/link";
import { type Product, productImage } from "@/lib/api";
import { formatPrice } from "@/lib/format";

export function ProductCardExpanded({ product }: { product: Product }) {
  const image = productImage(product);

  return (
    <Link
      href={`/produto/${product.productId}`}
      className="group flex flex-col overflow-hidden rounded-2xl border border-line bg-surface shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
    >
      <div className="relative aspect-square overflow-hidden bg-line">
        {image ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img
            src={image}
            alt={product.productName}
            loading="lazy"
            className="h-full w-full object-cover transition duration-300 group-hover:scale-105"
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-sm text-ink-soft">
            sem foto
          </div>
        )}
      </div>

      <div className="flex flex-1 flex-col gap-1 p-4">
        <h2 className="line-clamp-2 font-medium text-ink">{product.productName}</h2>
        <p className="font-display text-xl font-bold text-price">
          {formatPrice(product.price)}
        </p>
        {product.productDescription ? (
          <p className="line-clamp-2 text-sm text-ink-soft">
            {product.productDescription}
          </p>
        ) : null}
        <p className="mt-auto pt-2 text-xs text-ink-soft">@{product.owner.username}</p>
      </div>
    </Link>
  );
}
