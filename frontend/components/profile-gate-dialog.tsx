"use client";

import { useState } from "react";
import { ApiError, updateMe } from "@/lib/api";

export function ProfileGateDialog({
  token,
  initialWhatsapp,
  initialPix,
  onClose,
  onSaved,
}: {
  token: string | null;
  initialWhatsapp: string;
  initialPix: string;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [whatsapp, setWhatsapp] = useState(initialWhatsapp);
  const [pixKey, setPixKey] = useState(initialPix);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function save() {
    if (!token) return;
    if (!whatsapp.trim() || !pixKey.trim()) {
      setError("Preencha o WhatsApp e a chave Pix.");
      return;
    }
    setSaving(true);
    setError(null);
    try {
      await updateMe(token, { whatsapp: whatsapp.trim(), pixKey: pixKey.trim() });
      onSaved();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Não foi possível salvar.");
      setSaving(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <button
        type="button"
        aria-label="Fechar"
        onClick={onClose}
        className="absolute inset-0 bg-ink/40"
      />
      <div
        role="dialog"
        aria-modal="true"
        className="relative w-full max-w-sm rounded-2xl border border-line bg-surface p-6 shadow-xl"
      >
        <h2 className="font-display text-lg font-semibold text-ink">
          Falta pouco para vender
        </h2>
        <p className="mt-1 text-sm text-ink-soft">
          Para anunciar, diga como o comprador fala com você e como você recebe.
        </p>

        <div className="mt-4 flex flex-col gap-4">
          <label className="flex flex-col gap-1">
            <span className="text-sm font-medium text-ink">WhatsApp</span>
            <input
              value={whatsapp}
              onChange={(e) => setWhatsapp(e.target.value)}
              placeholder="5584999998888"
              inputMode="numeric"
              className="rounded-lg border border-line bg-surface px-3 py-2.5 text-sm text-ink placeholder:text-ink-soft focus-visible:border-action focus-visible:outline-none"
            />
            <span className="text-xs text-ink-soft">
              Só dígitos com DDI e DDD (12 ou 13 dígitos).
            </span>
          </label>
          <label className="flex flex-col gap-1">
            <span className="text-sm font-medium text-ink">Chave Pix</span>
            <input
              value={pixKey}
              onChange={(e) => setPixKey(e.target.value)}
              className="rounded-lg border border-line bg-surface px-3 py-2.5 text-sm text-ink placeholder:text-ink-soft focus-visible:border-action focus-visible:outline-none"
            />
            <span className="text-xs text-ink-soft">É como você recebe pelo produto.</span>
          </label>

          {error ? <p className="text-sm font-medium text-red-600">{error}</p> : null}

          <div className="flex gap-2">
            <button
              type="button"
              onClick={save}
              disabled={saving}
              className="flex-1 rounded-full bg-action px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-action-strong disabled:opacity-50"
            >
              {saving ? "Salvando…" : "Salvar e publicar"}
            </button>
            <button
              type="button"
              onClick={onClose}
              className="rounded-full border border-line bg-surface px-5 py-2.5 text-sm font-medium text-ink hover:bg-ink/5"
            >
              Cancelar
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
