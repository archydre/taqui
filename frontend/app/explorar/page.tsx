import { getProducts, type Product } from "@/lib/api";
import { ProductCard } from "@/components/product-card";

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
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
          {products.map((product) => (
            <ProductCard key={product.productId} product={product} />
          ))}
        </div>
      )}
    </div>
  );
}
