import { formatPrice } from "@/lib/format";

export function PriceTag({
  price,
  className = "",
}: {
  price: number;
  className?: string;
}) {
  return (
    <span
      className={`inline-flex items-center bg-price py-1 pr-3 font-display text-sm font-semibold text-white ${className}`}
      style={{
        clipPath: "polygon(12px 0, 100% 0, 100% 100%, 12px 100%, 0 50%)",
        paddingLeft: "1.25rem",
      }}
    >
      {formatPrice(price)}
    </span>
  );
}
