"use client";

import Link from "next/link";
import { useAuth } from "@/lib/auth";

export function BuyButton({
  productId,
  ownerUsername,
}: {
  productId: string;
  ownerUsername: string;
}) {
  const { user, loading } = useAuth();

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
    <Link
      href={`/produto/${productId}/comprar`}
      className="rounded-full bg-action px-6 py-3 text-center text-sm font-semibold text-white transition-colors hover:bg-action-strong"
    >
      Comprar
    </Link>
  );
}
