"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ApiError, createProduct } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { RequireAuth } from "@/components/require-auth";

export default function VenderPage() {
  return (
    <RequireAuth>
      <VenderForm />
    </RequireAuth>
  );
}

function VenderForm() {
  const router = useRouter();
  const { token } = useAuth();
  const [form, setForm] = useState({
    productName: "",
    productDescription: "",
    price: "",
    imageUrl: "",
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [missingProfile, setMissingProfile] = useState(false);

  function update(field: keyof typeof form) {
    return (
      e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
    ) => setForm((f) => ({ ...f, [field]: e.target.value }));
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!token) return;
    const price = Number(form.price.replace(",", "."));
    if (!Number.isFinite(price) || price <= 0) {
      setError("Informe um preço válido.");
      return;
    }
    setSubmitting(true);
    setError(null);
    setMissingProfile(false);
    try {
      const product = await createProduct(token, {
        productName: form.productName,
        productDescription: form.productDescription || undefined,
        price,
        imageUrl: form.imageUrl || undefined,
      });
      router.push(`/produto/${product.productId}`);
    } catch (err) {
      if (err instanceof ApiError && err.status === 422) {
        setMissingProfile(true);
        setError(err.message);
      } else {
        setError(err instanceof ApiError ? err.message : "Não deu para anunciar.");
      }
      setSubmitting(false);
    }
  }

  return (
    <div className="mx-auto w-full max-w-md py-8">
      <h1 className="font-display text-2xl font-semibold text-ink">Anunciar produto</h1>
      <p className="mt-1 text-ink-soft">Coloque algo à venda na vitrine.</p>

      <form onSubmit={onSubmit} className="mt-6 flex flex-col gap-4">
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-ink">Nome do produto</span>
          <input
            required
            maxLength={100}
            value={form.productName}
            onChange={update("productName")}
            className="rounded-lg border border-line bg-surface px-3 py-2.5 text-sm text-ink focus-visible:border-action focus-visible:outline-none"
          />
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-ink">Descrição</span>
          <textarea
            maxLength={500}
            rows={3}
            value={form.productDescription}
            onChange={update("productDescription")}
            className="resize-none rounded-lg border border-line bg-surface px-3 py-2.5 text-sm text-ink focus-visible:border-action focus-visible:outline-none"
          />
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-ink">Preço (R$)</span>
          <input
            required
            inputMode="decimal"
            value={form.price}
            onChange={update("price")}
            placeholder="0,00"
            className="rounded-lg border border-line bg-surface px-3 py-2.5 text-sm text-ink focus-visible:border-action focus-visible:outline-none"
          />
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-ink">URL da imagem</span>
          <input
            type="url"
            value={form.imageUrl}
            onChange={update("imageUrl")}
            placeholder="https://…"
            className="rounded-lg border border-line bg-surface px-3 py-2.5 text-sm text-ink placeholder:text-ink-soft focus-visible:border-action focus-visible:outline-none"
          />
        </label>

        {error ? <p className="text-sm text-red-600">{error}</p> : null}
        {missingProfile ? (
          <p className="text-sm text-ink-soft">
            Complete seu{" "}
            <Link href="/perfil" className="font-medium text-action hover:underline">
              perfil
            </Link>{" "}
            (WhatsApp e chave Pix) para poder vender.
          </p>
        ) : null}

        <button
          type="submit"
          disabled={submitting}
          className="rounded-full bg-action px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-action-strong disabled:opacity-50"
        >
          {submitting ? "Publicando…" : "Publicar anúncio"}
        </button>
      </form>
    </div>
  );
}
