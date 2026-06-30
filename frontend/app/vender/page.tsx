"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ApiError, createProduct } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { RequireAuth } from "@/components/require-auth";
import { ImagePicker } from "@/components/image-picker";

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
    thumbnailUrl: "",
    weight: "",
    width: "",
    height: "",
    length: "",
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [missingProfile, setMissingProfile] = useState(false);

  function update(field: keyof typeof form) {
    return (
      e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
    ) => setForm((f) => ({ ...f, [field]: e.target.value }));
  }

  function optionalNumber(value: string): number | undefined {
    const n = Number(value.replace(",", "."));
    return Number.isFinite(n) && n > 0 ? n : undefined;
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
        thumbnailUrl: form.thumbnailUrl || undefined,
        weight: optionalNumber(form.weight),
        width: optionalNumber(form.width),
        height: optionalNumber(form.height),
        length: optionalNumber(form.length),
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
        <div className="flex flex-col gap-1">
          <span className="text-sm font-medium text-ink">Imagem</span>
          <ImagePicker
            token={token}
            imageUrl={form.imageUrl || null}
            onUploaded={(urls) =>
              setForm((f) => ({
                ...f,
                imageUrl: urls.imageUrl,
                thumbnailUrl: urls.thumbnailUrl,
              }))
            }
            onClear={() =>
              setForm((f) => ({ ...f, imageUrl: "", thumbnailUrl: "" }))
            }
          />
        </div>

        <fieldset className="rounded-lg border border-line p-3">
          <legend className="px-1 text-sm font-medium text-ink">
            Dimensões para frete{" "}
            <span className="font-normal text-ink-soft">(opcional)</span>
          </legend>
          <p className="mb-2 text-xs text-ink-soft">
            Preencha para calcular o frete na compra.
          </p>
          <div className="grid grid-cols-2 gap-3">
            <DimField label="Peso (kg)" value={form.weight} onChange={update("weight")} />
            <DimField label="Largura (cm)" value={form.width} onChange={update("width")} />
            <DimField label="Altura (cm)" value={form.height} onChange={update("height")} />
            <DimField label="Comprimento (cm)" value={form.length} onChange={update("length")} />
          </div>
        </fieldset>

        {error ? <p className="text-sm font-medium text-slate-700">{error}</p> : null}
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

function DimField({
  label,
  value,
  onChange,
}: {
  label: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}) {
  return (
    <label className="flex flex-col gap-1">
      <span className="text-xs font-medium text-ink-soft">{label}</span>
      <input
        inputMode="decimal"
        value={value}
        onChange={onChange}
        placeholder="0"
        className="rounded-lg border border-line bg-surface px-3 py-2 text-sm text-ink focus-visible:border-action focus-visible:outline-none"
      />
    </label>
  );
}
