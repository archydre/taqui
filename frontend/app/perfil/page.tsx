"use client";

import { useState } from "react";
import { ApiError, updateMe } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { RequireAuth } from "@/components/require-auth";

export default function PerfilPage() {
  return (
    <RequireAuth>
      <PerfilForm />
    </RequireAuth>
  );
}

function PerfilForm() {
  const { user, token, refresh } = useAuth();
  const [form, setForm] = useState({
    displayName: user?.displayName ?? "",
    whatsapp: user?.whatsapp ?? "",
    pixKey: user?.pixKey ?? "",
    postalCode: user?.postalCode ?? "",
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  function update(field: keyof typeof form) {
    return (e: React.ChangeEvent<HTMLInputElement>) => {
      setForm((f) => ({ ...f, [field]: e.target.value }));
      setSaved(false);
    };
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!token) return;
    setSaving(true);
    setError(null);
    setSaved(false);
    try {
      await updateMe(token, {
        displayName: form.displayName || undefined,
        whatsapp: form.whatsapp || undefined,
        pixKey: form.pixKey || undefined,
        postalCode: form.postalCode || undefined,
      });
      await refresh();
      setSaved(true);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Não deu para salvar.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="mx-auto w-full max-w-md py-8">
      <h1 className="font-display text-2xl font-semibold text-ink">Editar perfil</h1>
      <p className="mt-1 text-ink-soft">
        @{user?.username} · {user?.email}
      </p>

      <form onSubmit={onSubmit} className="mt-6 flex flex-col gap-4">
        <Field label="Nome" value={form.displayName} onChange={update("displayName")} />
        <Field
          label="WhatsApp"
          value={form.whatsapp}
          onChange={update("whatsapp")}
          placeholder="5584999998888"
          hint="Só dígitos com DDI e DDD (12 ou 13 dígitos). Obrigatório para vender."
        />
        <Field
          label="Chave Pix"
          value={form.pixKey}
          onChange={update("pixKey")}
          hint="Obrigatória para vender — é como você recebe."
        />
        <Field
          label="CEP de origem"
          value={form.postalCode}
          onChange={update("postalCode")}
          placeholder="59600000"
          hint="8 dígitos. Usado para calcular o frete dos seus produtos."
        />

        {error ? <p className="text-sm text-red-600">{error}</p> : null}
        {saved ? <p className="text-sm text-price">Perfil salvo.</p> : null}

        <button
          type="submit"
          disabled={saving}
          className="rounded-full bg-action px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-action-strong disabled:opacity-50"
        >
          {saving ? "Salvando…" : "Salvar"}
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
  hint,
}: {
  label: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  placeholder?: string;
  hint?: string;
}) {
  return (
    <label className="flex flex-col gap-1">
      <span className="text-sm font-medium text-ink">{label}</span>
      <input
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        className="rounded-lg border border-line bg-surface px-3 py-2.5 text-sm text-ink placeholder:text-ink-soft focus-visible:border-action focus-visible:outline-none"
      />
      {hint ? <span className="text-xs text-ink-soft">{hint}</span> : null}
    </label>
  );
}
