"use client";

import { useState, useEffect } from "react";
import { useRouter, useParams } from "next/navigation";
import {
  ApiError,
  getProductById,
  updateProduct,
  type Product,
} from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { RequireAuth } from "@/components/require-auth";
import { ImagePicker } from "@/components/image-picker";

export default function EditarProdutoPage() {
  return (
    <RequireAuth>
      <EditarForm />
    </RequireAuth>
  );
}

function EditarForm() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const { user, token } = useAuth();

  const [product, setProduct] = useState<Product | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
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
  const [imageUploading, setImageUploading] = useState(false);

  useEffect(() => {
    if (!id) return;
    getProductById(id)
      .then((p) => {
        if (user && user.username !== p.owner.username) {
          router.replace(`/produto/${id}`);
          return;
        }
        setProduct(p);
        setForm({
          productName: p.productName,
          productDescription: p.productDescription ?? "",
          price: String(p.price),
          imageUrl: p.imageUrl ?? "",
          thumbnailUrl: p.thumbnailUrl ?? "",
          weight: p.weight != null ? String(p.weight) : "",
          width: p.width != null ? String(p.width) : "",
          height: p.height != null ? String(p.height) : "",
          length: p.length != null ? String(p.length) : "",
        });
      })
      .catch((err) => {
        setLoadError(
          err instanceof ApiError && err.status === 404
            ? "Produto não encontrado."
            : "Não foi possível carregar o produto.",
        );
      });
  }, [id, user, router]);

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
    if (!token || !product) return;
    if (imageUploading) {
      setError("Aguarde o envio da imagem terminar.");
      return;
    }
    const price = Number(form.price.replace(",", "."));
    if (!Number.isFinite(price) || price <= 0) {
      setError("Informe um preço válido.");
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await updateProduct(token, product.productId, {
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
      setError(
        err instanceof ApiError ? err.message : "Não foi possível salvar as alterações.",
      );
      setSubmitting(false);
    }
  }

  if (loadError) {
    return (
      <div className="mx-auto w-full max-w-md py-10 text-center">
        <p className="text-ink-soft">{loadError}</p>
      </div>
    );
  }

  if (!product) {
    return <div className="mx-auto w-full max-w-md py-10" />;
  }

  return (
    <div className="mx-auto w-full max-w-md py-8">
      <h1 className="font-display text-2xl font-semibold text-ink">Editar produto</h1>
      <p className="mt-1 text-ink-soft">Atualize as informações do anúncio.</p>

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
            onUploadingChange={setImageUploading}
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

        {error ? <p className="text-sm font-medium text-red-600">{error}</p> : null}

        <div className="flex gap-3">
          <button
            type="submit"
            disabled={submitting || imageUploading}
            className="flex-1 rounded-full bg-action px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-action-strong disabled:opacity-50"
          >
            {imageUploading
              ? "Enviando imagem…"
              : submitting
                ? "Salvando…"
                : "Salvar alterações"}
          </button>
          <button
            type="button"
            onClick={() => router.back()}
            className="rounded-full border border-line px-5 py-2.5 text-sm font-medium text-ink hover:bg-ink/5"
          >
            Cancelar
          </button>
        </div>
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
