"use client";

import { useState } from "react";
import Link from "next/link";
import { ApiError, revealWhatsapp } from "@/lib/api";
import { useAuth } from "@/lib/auth";

export function RevealWhatsapp({ username }: { username: string }) {
  const { token, loading } = useAuth();
  const [number, setNumber] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (loading) return null;

  if (!token) {
    return (
      <Link
        href="/entrar"
        className="inline-flex rounded-full border border-line px-4 py-2 text-sm font-medium text-ink hover:bg-ink/5"
      >
        Entre para ver o WhatsApp
      </Link>
    );
  }

  if (number) {
    const digits = number.replace(/\D/g, "");
    return (
      <a
        href={`https://wa.me/${digits}`}
        target="_blank"
        rel="noopener noreferrer"
        className="inline-flex rounded-full bg-price px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
      >
        Conversar no WhatsApp
      </a>
    );
  }

  async function reveal() {
    if (!token) return;
    setBusy(true);
    setError(null);
    try {
      const res = await revealWhatsapp(token, username);
      setNumber(res.whatsapp);
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Não deu para mostrar o número.",
      );
    } finally {
      setBusy(false);
    }
  }

  return (
    <div>
      <button
        type="button"
        onClick={reveal}
        disabled={busy}
        className="inline-flex rounded-full border border-line px-4 py-2 text-sm font-medium text-ink hover:bg-ink/5 disabled:opacity-50"
      >
        {busy ? "Mostrando…" : "Mostrar WhatsApp"}
      </button>
      {error ? <p className="mt-2 text-sm text-red-600">{error}</p> : null}
    </div>
  );
}
