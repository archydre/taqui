import Link from "next/link";
import { type Product, productImage } from "@/lib/api";
import { PriceTag } from "./price-tag";

export function ProductAttachment({ product }: { product: Product }) {
  const image = productImage(product);

  return (
    <Link
      href={`/produto/${product.productId}`}
      className="mt-3 flex items-center gap-3 rounded-xl border border-line bg-canvas p-3 transition hover:border-action"
    >
      <div className="h-16 w-16 shrink-0 overflow-hidden rounded-lg bg-line">
        {image ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img
            src={image}
            alt={product.productName}
            loading="lazy"
            className="h-full w-full object-cover"
          />
        ) : null}
      </div>
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-medium text-ink">
          {product.productName}
        </p>
        <p className="mt-1">
          <PriceTag price={product.price} />
        </p>
      </div>
    </Link>
  );
}
