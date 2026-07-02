"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { addToCart, ApiError } from "@/lib/api";
import { useAuth } from "@/lib/auth";

export function BuyButton({
  productId,
  ownerUsername,
}: {
  productId: string;
  ownerUsername: string;
}) {
  const { user, loading, token } = useAuth();

  if (loading) {
    return (
      <div
        className="h-11 w-full animate-pulse rounded-full bg-line"
        aria-hidden="true"
      />
    );
  }

  if (user && user.username === ownerUsername) return null;

  return (
    <>
      <Link
        href={`/produto/${productId}/comprar`}
        className="rounded-full bg-action px-6 py-3 text-center text-sm font-semibold text-white transition-colors hover:bg-action-strong"
      >
        Comprar
      </Link>
      <AddToCartButton productId={productId} token={token} />
    </>
  );
}

type CartState = "idle" | "adding" | "added" | "error";

function AddToCartButton({
  productId,
  token,
}: {
  productId: string;
  token: string | null;
}) {
  const router = useRouter();
  const [state, setState] = useState<CartState>("idle");
  const [error, setError] = useState<string | null>(null);

  async function handleAdd() {
    if (!token) {
      router.push("/entrar");
      return;
    }
    setState("adding");
    setError(null);
    try {
      await addToCart(token, { productId, quantity: 1 });
      setState("added");
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : "Não deu para adicionar ao carrinho.",
      );
      setState("error");
    }
  }

  if (state === "added") {
    return (
      <Link
        href="/carrinho"
        className="rounded-full border border-line px-6 py-3 text-center text-sm font-semibold text-ink transition-colors hover:bg-ink/5"
      >
        Adicionado ✓ — ver carrinho
      </Link>
    );
  }

  return (
    <>
      <button
        type="button"
        onClick={handleAdd}
        disabled={state === "adding"}
        className="rounded-full border border-line px-6 py-3 text-center text-sm font-semibold text-ink transition-colors hover:bg-ink/5 disabled:opacity-50"
      >
        {state === "adding" ? "Adicionando…" : "Adicionar ao carrinho"}
      </button>
      {error ? <p className="text-sm text-ink-soft">{error}</p> : null}
    </>
  );
}
