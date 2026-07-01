import Link from "next/link";
import { type Product, productImage } from "@/lib/api";
import { PriceTag } from "./price-tag";
import { IconComment } from "./icons";

export function ProductCard({
  product,
  commentCount,
}: {
  product: Product;
  commentCount?: number;
}) {
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
        {commentCount !== undefined ? (
          <div className="absolute right-2 top-2 flex items-center gap-1 rounded-full bg-ink/55 px-2 py-1 text-xs font-medium text-white backdrop-blur-sm">
            <IconComment className="h-3.5 w-3.5" />
            {commentCount}
          </div>
        ) : null}
        <div className="absolute bottom-2 left-0">
          <PriceTag price={product.price} />
        </div>
      </div>

      <div className="flex flex-1 flex-col gap-1 p-4">
        <h2 className="line-clamp-2 text-sm font-medium text-ink">
          {product.productName}
        </h2>
        <p className="mt-auto pt-1 text-xs text-ink-soft">
          @{product.owner.username}
        </p>
      </div>
    </Link>
  );
}
