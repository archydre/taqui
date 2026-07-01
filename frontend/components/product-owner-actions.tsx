"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { deleteProduct } from "@/lib/api";
import { useAuth } from "@/lib/auth";

export function ProductOwnerActions({
  productId,
  ownerUsername,
}: {
  productId: string;
  ownerUsername: string;
}) {
  const { user, token } = useAuth();
  const router = useRouter();
  const [deleting, setDeleting] = useState(false);

  if (!user || user.username !== ownerUsername) return null;

  async function handleDelete() {
    if (!token) return;
    if (!confirm("Excluir este produto? Esta ação não pode ser desfeita.")) return;
    setDeleting(true);
    try {
      await deleteProduct(token, productId);
      router.push("/explorar");
    } catch {
      alert("Não foi possível excluir o produto.");
      setDeleting(false);
    }
  }

  return (
    <div className="flex gap-2">
      <Link
        href={`/produto/${productId}/editar`}
        className="rounded-full border border-line bg-surface px-4 py-2.5 text-center text-sm font-medium text-ink hover:bg-ink/5"
      >
        Editar
      </Link>
      <button
        type="button"
        onClick={handleDelete}
        disabled={deleting}
        className="rounded-full border border-red-200 bg-surface px-4 py-2.5 text-sm font-medium text-red-600 hover:bg-red-50 disabled:opacity-50"
      >
        {deleting ? "Excluindo…" : "Excluir"}
      </button>
    </div>
  );
}
