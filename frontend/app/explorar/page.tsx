import { getProducts, type Product } from "@/lib/api";
import { ProductCardExpanded } from "@/components/product-card-expanded";

export default async function Explorar({
  searchParams,
}: {
  searchParams: Promise<{ q?: string }>;
}) {
  const { q } = await searchParams;

  let products: Product[] = [];
  let error = false;

  try {
    const page = await getProducts({ q, size: 24 });
    products = page.content;
  } catch {
    error = true;
  }

  return (
    <div>
      <header className="mb-6">
        <h1 className="font-display text-2xl font-semibold text-ink">
          {q ? `Resultados para “${q}”` : "Explorar"}
        </h1>
        <p className="mt-1 text-ink-soft">
          {q
            ? "Produtos que combinam com a sua busca."
            : "Tudo que está à venda na vitrine."}
        </p>
      </header>

      {error ? (
        <p className="rounded-xl border border-slate-300 bg-slate-100 px-4 py-3 text-sm text-slate-700">
          Não foi possível carregar a vitrine. A API está no ar?
        </p>
      ) : products.length === 0 ? (
        <p className="text-ink-soft">
          {q
            ? "Nada encontrado. Tente outro termo."
            : "Nenhum produto na vitrine ainda."}
        </p>
      ) : (
        <div className="grid grid-cols-2 gap-5 lg:grid-cols-3">
          {products.map((product, i) => (
            <div
              key={product.productId}
              className="animate-fade-in-up"
              style={{ animationDelay: `${Math.min(i, 8) * 50}ms` }}
            >
              <ProductCardExpanded product={product} />
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
