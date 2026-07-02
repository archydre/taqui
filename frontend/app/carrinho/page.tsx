"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import {
  getCart,
  removeFromCart,
  updateCartItem,
  type CartItem,
} from "@/lib/api";
import { formatPrice } from "@/lib/format";
import { useAuth } from "@/lib/auth";
import { RequireAuth } from "@/components/require-auth";

export default function CarrinhoPage() {
  return (
    <RequireAuth>
      <Carrinho />
    </RequireAuth>
  );
}

type SellerGroup = { username: string; displayName: string; items: CartItem[] };

// Um vendedor por seção: cada um tem Pix e frete próprios, então a compra é finalizada separada.
function groupBySeller(items: CartItem[]): SellerGroup[] {
  const map = new Map<string, SellerGroup>();
  for (const item of items) {
    const owner = item.product.owner;
    const group = map.get(owner.username) ?? {
      username: owner.username,
      displayName: owner.displayName,
      items: [],
    };
    group.items.push(item);
    map.set(owner.username, group);
  }
  return [...map.values()];
}

function Carrinho() {
  const { token } = useAuth();
  const [items, setItems] = useState<CartItem[] | null>(null);
  const [error, setError] = useState(false);
  const [busyId, setBusyId] = useState<string | null>(null);

  useEffect(() => {
    if (!token) return;
    getCart(token)
      .then(setItems)
      .catch(() => setError(true));
  }, [token]);

  const setQuantity = useCallback(
    async (item: CartItem, quantity: number) => {
      if (!token) return;
      setBusyId(item.id);
      try {
        if (quantity <= 0) {
          await removeFromCart(token, item.product.productId);
          setItems((cur) => (cur ?? []).filter((i) => i.id !== item.id));
        } else {
          const updated = await updateCartItem(token, item.product.productId, quantity);
          setItems((cur) => (cur ?? []).map((i) => (i.id === item.id ? updated : i)));
        }
      } catch {
        setError(true);
      } finally {
        setBusyId(null);
      }
    },
    [token],
  );

  if (error) {
    return <p className="py-8 text-ink-soft">Não foi possível carregar seu carrinho.</p>;
  }
  if (items === null) {
    return <p className="py-8 text-ink-soft">Carregando…</p>;
  }

  const total = items.reduce((sum, i) => sum + i.product.price * i.quantity, 0);
  const groups = groupBySeller(items);

  return (
    <div className="mx-auto w-full max-w-2xl py-4">
      <h1 className="font-display text-2xl font-semibold text-ink">Carrinho</h1>

      {items.length === 0 ? (
        <p className="mt-4 text-ink-soft">
          Seu carrinho está vazio.{" "}
          <Link href="/explorar" className="font-medium text-action hover:underline">
            Explorar produtos
          </Link>
        </p>
      ) : (
        <>
          <p className="mt-1 text-sm text-ink-soft">
            Cada vendedor tem seu próprio Pix e frete — a compra é finalizada por vendedor.
          </p>

          <div className="mt-6 flex flex-col gap-6">
            {groups.map((group) => (
              <section key={group.username}>
                <Link
                  href={`/u/${group.username}`}
                  className="text-sm font-medium text-ink hover:underline"
                >
                  {group.displayName}
                </Link>
                <ul className="mt-2 flex flex-col gap-3">
                  {group.items.map((item) => (
                    <CartRow
                      key={item.id}
                      item={item}
                      busy={busyId === item.id}
                      onQuantity={(q) => setQuantity(item, q)}
                    />
                  ))}
                </ul>
              </section>
            ))}
          </div>

          <div className="mt-8 flex items-center justify-between border-t border-line pt-4">
            <span className="text-ink-soft">Total dos produtos</span>
            <span className="font-display text-xl font-bold text-ink">
              {formatPrice(total)}
            </span>
          </div>
          <p className="mt-1 text-right text-xs text-ink-soft">
            Frete calculado no checkout de cada vendedor.
          </p>
        </>
      )}
    </div>
  );
}

function CartRow({
  item,
  busy,
  onQuantity,
}: {
  item: CartItem;
  busy: boolean;
  onQuantity: (quantity: number) => void;
}) {
  const { product, quantity } = item;
  const thumb = product.thumbnailUrl ?? product.imageUrl;

  return (
    <li className="flex gap-3 rounded-xl border border-line bg-surface p-3">
      <div className="grid h-16 w-16 shrink-0 place-items-center overflow-hidden rounded-lg bg-line text-xs text-ink-soft">
        {thumb ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img
            src={thumb}
            alt={product.productName}
            className="h-full w-full object-cover"
          />
        ) : (
          "sem foto"
        )}
      </div>

      <div className="flex min-w-0 flex-1 flex-col">
        <div className="flex items-start justify-between gap-2">
          <Link
            href={`/produto/${product.productId}`}
            className="truncate font-medium text-ink hover:underline"
          >
            {product.productName}
          </Link>
          <button
            type="button"
            onClick={() => onQuantity(0)}
            disabled={busy}
            className="shrink-0 text-sm text-ink-soft hover:text-ink disabled:opacity-50"
          >
            Remover
          </button>
        </div>

        <span className="mt-0.5 text-sm font-medium text-price">
          {formatPrice(product.price)}
        </span>

        <div className="mt-2 flex items-center justify-between gap-2">
          <div className="flex items-center gap-3">
            <div className="flex items-center rounded-full border border-line">
              <button
                type="button"
                onClick={() => onQuantity(quantity - 1)}
                disabled={busy}
                aria-label="Diminuir quantidade"
                className="grid h-8 w-8 place-items-center rounded-full text-ink hover:bg-ink/5 disabled:opacity-50"
              >
                −
              </button>
              <span className="w-8 text-center text-sm text-ink">{quantity}</span>
              <button
                type="button"
                onClick={() => onQuantity(quantity + 1)}
                disabled={busy}
                aria-label="Aumentar quantidade"
                className="grid h-8 w-8 place-items-center rounded-full text-ink hover:bg-ink/5 disabled:opacity-50"
              >
                +
              </button>
            </div>
            <span className="text-sm text-ink-soft">
              {formatPrice(product.price * quantity)}
            </span>
          </div>

          <Link
            href={`/produto/${product.productId}/comprar`}
            className="rounded-full bg-action px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-action-strong"
          >
            Comprar
          </Link>
        </div>
      </div>
    </li>
  );
}
