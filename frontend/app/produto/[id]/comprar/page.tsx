"use client";

import { use, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  ApiError,
  createAddress,
  createOrder,
  getMyAddresses,
  getProductById,
  quoteFreightForProduct,
  type FreightOption,
  type Product,
  type SavedAddress,
} from "@/lib/api";
import { formatPrice } from "@/lib/format";
import { useAuth } from "@/lib/auth";
import { RequireAuth } from "@/components/require-auth";

type FreightChoice = { service: string; price: number; label: string };

const FALLBACK_FREIGHT: FreightChoice = {
  service: "Combinar com o vendedor",
  price: 0,
  label: "Combinar com o vendedor — grátis",
};

const EMPTY_ADDRESS = {
  recipientName: "",
  postalCode: "",
  street: "",
  number: "",
  complement: "",
  district: "",
  city: "",
  state: "",
};

// "new" = digitar um endereço novo; qualquer outro valor = id de um endereço salvo.
const NEW_ADDRESS = "new";

function formatCep(cep: string): string {
  const digits = cep.replace(/\D/g, "");
  return digits.length === 8 ? `${digits.slice(0, 5)}-${digits.slice(5)}` : cep;
}

function pickAddressFields(saved: SavedAddress): typeof EMPTY_ADDRESS {
  return {
    recipientName: saved.recipientName,
    postalCode: saved.postalCode,
    street: saved.street,
    number: saved.number,
    complement: saved.complement ?? "",
    district: saved.district,
    city: saved.city,
    state: saved.state,
  };
}

export default function ComprarPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  return (
    <RequireAuth>
      <Checkout productId={id} />
    </RequireAuth>
  );
}

function Checkout({ productId }: { productId: string }) {
  const router = useRouter();
  const { token, user } = useAuth();

  const [product, setProduct] = useState<Product | null>(null);
  const [loadError, setLoadError] = useState(false);
  const isOwner = !!product && !!user && user.username === product.owner.username;
  const [quantity, setQuantity] = useState(1);
  const [address, setAddress] = useState({ ...EMPTY_ADDRESS });

  const [savedAddresses, setSavedAddresses] = useState<SavedAddress[]>([]);
  const [selectedId, setSelectedId] = useState<string>(NEW_ADDRESS);
  const [saveNew, setSaveNew] = useState(true);

  const [options, setOptions] = useState<FreightOption[] | null>(null);
  const [quoting, setQuoting] = useState(false);
  const [freightError, setFreightError] = useState<string | null>(null);
  const [choice, setChoice] = useState<FreightChoice | null>(null);

  const [placing, setPlacing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getProductById(productId)
      .then(setProduct)
      .catch(() => setLoadError(true));
  }, [productId]);

  useEffect(() => {
    if (isOwner) router.replace(`/produto/${productId}`);
  }, [isOwner, productId, router]);

  useEffect(() => {
    if (!token) return;
    getMyAddresses(token)
      .then((list) => {
        setSavedAddresses(list);
        if (list.length > 0) {
          const first = list[0];
          setSelectedId(first.id);
          setAddress(pickAddressFields(first));
        }
      })
      .catch(() => {
        // sem endereços salvos (ou falha ao buscar): segue com o formulário em branco
      });
  }, [token]);

  // Trocar o endereço muda o CEP, então a cotação de frete anterior deixa de valer.
  function resetFreight() {
    setOptions(null);
    setChoice(null);
    setFreightError(null);
  }

  function selectSaved(saved: SavedAddress) {
    setSelectedId(saved.id);
    setAddress(pickAddressFields(saved));
    resetFreight();
  }

  function selectNew() {
    setSelectedId(NEW_ADDRESS);
    setAddress({ ...EMPTY_ADDRESS });
    resetFreight();
  }

  function updateAddress(field: keyof typeof address) {
    return (e: React.ChangeEvent<HTMLInputElement>) =>
      setAddress((a) => ({ ...a, [field]: e.target.value }));
  }

  const cepDigits = address.postalCode.replace(/\D/g, "");

  async function calcFreight() {
    if (!token) return;
    setQuoting(true);
    setFreightError(null);
    setOptions(null);
    try {
      const result = await quoteFreightForProduct(token, productId, cepDigits, quantity);
      setOptions(result);
    } catch (err) {
      setFreightError(
        err instanceof ApiError
          ? err.message
          : "Não foi possível calcular o frete agora.",
      );
      setOptions([]);
    } finally {
      setQuoting(false);
    }
  }

  const choices: FreightChoice[] = [
    ...(options ?? []).map((o) => {
      const price = Number(o.customPrice ?? o.price ?? "0");
      const days = o.deliveryTime ? ` · ${o.deliveryTime} dias` : "";
      const company = o.company?.name ? `${o.company.name} ` : "";
      return {
        service: `${company}${o.name}`.trim(),
        price,
        label: `${company}${o.name} — ${formatPrice(price)}${days}`,
      };
    }),
    FALLBACK_FREIGHT,
  ];

  const unitPrice = product?.price ?? 0;
  const total = unitPrice * quantity + (choice?.price ?? 0);

  async function placeOrder(e: React.FormEvent) {
    e.preventDefault();
    if (!token || !product) return;
    if (!choice) {
      setError("Escolha uma opção de frete.");
      return;
    }
    setPlacing(true);
    setError(null);
    try {
      const order = await createOrder(token, {
        productId,
        quantity,
        freightService: choice.service,
        freightPrice: choice.price,
        address: { ...address, postalCode: cepDigits },
      });
      if (selectedId === NEW_ADDRESS && saveNew) {
        try {
          await createAddress(token, { ...address, postalCode: cepDigits });
        } catch {
          // salvar o endereço é secundário; o pedido já foi criado
        }
      }
      router.push(`/pedidos/${order.orderId}`);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Não deu para finalizar o pedido.");
      setPlacing(false);
    }
  }

  if (loadError) {
    return <p className="py-8 text-ink-soft">Não foi possível carregar o produto.</p>;
  }
  if (!product || isOwner) {
    return <p className="py-8 text-ink-soft">Carregando…</p>;
  }

  return (
    <div className="mx-auto w-full max-w-lg py-6">
      <h1 className="font-display text-2xl font-semibold text-ink">Finalizar compra</h1>

      <div className="mt-4 flex items-center justify-between rounded-xl border border-line bg-surface p-4">
        <span className="font-medium text-ink">{product.productName}</span>
        <span className="font-display font-semibold text-ink">
          {formatPrice(product.price)}
        </span>
      </div>

      <form onSubmit={placeOrder} className="mt-6 flex flex-col gap-4">
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-ink">Quantidade</span>
          <input
            type="number"
            min={1}
            value={quantity}
            onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}
            className="w-24 rounded-lg border border-line bg-surface px-3 py-2.5 text-sm text-ink focus-visible:border-action focus-visible:outline-none"
          />
        </label>

        <h2 className="mt-2 font-display text-lg font-semibold text-ink">
          Endereço de entrega
        </h2>

        {savedAddresses.length > 0 ? (
          <div className="flex flex-col gap-2">
            {savedAddresses.map((saved) => (
              <button
                key={saved.id}
                type="button"
                onClick={() => selectSaved(saved)}
                className={`flex flex-col items-start rounded-lg border px-3 py-2.5 text-left text-sm transition-colors ${
                  selectedId === saved.id
                    ? "border-action bg-action/5"
                    : "border-line bg-surface hover:bg-ink/5"
                }`}
              >
                <span className="font-medium text-ink">{saved.recipientName}</span>
                <span className="text-ink-soft">
                  {formatCep(saved.postalCode)} · {saved.street}
                  {saved.number ? `, ${saved.number}` : ""}
                </span>
              </button>
            ))}
            <button
              type="button"
              onClick={selectNew}
              className={`rounded-lg border px-3 py-2.5 text-left text-sm font-medium transition-colors ${
                selectedId === NEW_ADDRESS
                  ? "border-action bg-action/5 text-ink"
                  : "border-line bg-surface text-ink-soft hover:bg-ink/5"
              }`}
            >
              + Usar outro endereço
            </button>
          </div>
        ) : null}

        {selectedId === NEW_ADDRESS ? (
          <>
            <Field label="Nome de quem recebe" value={address.recipientName} onChange={updateAddress("recipientName")} required />
            <div className="grid grid-cols-2 gap-3">
              <Field label="CEP" value={address.postalCode} onChange={updateAddress("postalCode")} placeholder="59600000" required />
              <Field label="Número" value={address.number} onChange={updateAddress("number")} required />
            </div>
            <Field label="Rua" value={address.street} onChange={updateAddress("street")} required />
            <Field label="Complemento" value={address.complement} onChange={updateAddress("complement")} />
            <Field label="Bairro" value={address.district} onChange={updateAddress("district")} required />
            <div className="grid grid-cols-[1fr_5rem] gap-3">
              <Field label="Cidade" value={address.city} onChange={updateAddress("city")} required />
              <Field label="UF" value={address.state} onChange={updateAddress("state")} placeholder="RN" required />
            </div>
            <label className="flex items-center gap-2 text-sm text-ink">
              <input
                type="checkbox"
                checked={saveNew}
                onChange={(e) => setSaveNew(e.target.checked)}
                className="accent-[var(--color-action)]"
              />
              Salvar este endereço para a próxima compra
            </label>
          </>
        ) : null}

        <div className="mt-2">
          <button
            type="button"
            onClick={calcFreight}
            disabled={quoting || cepDigits.length !== 8}
            className="rounded-full border border-line px-4 py-2 text-sm font-medium text-ink hover:bg-ink/5 disabled:opacity-50"
          >
            {quoting ? "Calculando…" : "Calcular frete"}
          </button>
          {cepDigits.length !== 8 ? (
            <span className="ml-2 text-xs text-ink-soft">Informe um CEP de 8 dígitos.</span>
          ) : null}
        </div>

        {freightError ? (
          <p className="text-sm text-ink-soft">
            {freightError} Você pode combinar o frete direto com o vendedor.
          </p>
        ) : null}

        {options !== null ? (
          <fieldset className="flex flex-col gap-2">
            <legend className="mb-1 text-sm font-medium text-ink">Frete</legend>
            {choices.map((c) => (
              <label
                key={c.service}
                className="flex cursor-pointer items-center gap-3 rounded-lg border border-line bg-surface px-3 py-2.5 text-sm has-[:checked]:border-action"
              >
                <input
                  type="radio"
                  name="freight"
                  checked={choice?.service === c.service}
                  onChange={() => setChoice(c)}
                  className="accent-[var(--color-action)]"
                />
                <span className="text-ink">{c.label}</span>
              </label>
            ))}
          </fieldset>
        ) : null}

        <div className="mt-2 flex items-center justify-between border-t border-line pt-4">
          <span className="text-ink-soft">Total</span>
          <span className="font-display text-xl font-bold text-ink">
            {formatPrice(total)}
          </span>
        </div>

        {error ? <p className="text-sm font-medium text-slate-700">{error}</p> : null}

        <button
          type="submit"
          disabled={placing}
          className="rounded-full bg-action px-6 py-3 text-sm font-semibold text-white transition-colors hover:bg-action-strong disabled:opacity-50"
        >
          {placing ? "Finalizando…" : "Finalizar pedido"}
        </button>
      </form>
    </div>
  );
}

function Field({
  label,
  value,
  onChange,
  placeholder,
  required,
}: {
  label: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  placeholder?: string;
  required?: boolean;
}) {
  return (
    <label className="flex flex-col gap-1">
      <span className="text-sm font-medium text-ink">{label}</span>
      <input
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        className="rounded-lg border border-line bg-surface px-3 py-2.5 text-sm text-ink placeholder:text-ink-soft focus-visible:border-action focus-visible:outline-none"
      />
    </label>
  );
}
