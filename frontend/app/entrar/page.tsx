"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ApiError, login as loginRequest } from "@/lib/api";
import { useAuth } from "@/lib/auth";

export default function EntrarPage() {
  const router = useRouter();
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const { token } = await loginRequest(email, password);
      await login(token);
      router.push("/");
    } catch (err) {
      setError(
        err instanceof ApiError && err.status === 401
          ? "E-mail ou senha incorretos."
          : "Não foi possível entrar. Tente de novo.",
      );
      setSubmitting(false);
    }
  }

  return (
    <div className="mx-auto w-full max-w-sm py-8">
      <h1 className="font-display text-2xl font-semibold text-ink">Entrar</h1>
      <p className="mt-1 text-ink-soft">Bem-vindo de volta ao taqui.</p>

      <form onSubmit={onSubmit} className="mt-6 flex flex-col gap-4">
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-ink">E-mail</span>
          <input
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="rounded-lg border border-line bg-surface px-3 py-2.5 text-sm text-ink focus-visible:border-action focus-visible:outline-none"
          />
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-ink">Senha</span>
          <input
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="rounded-lg border border-line bg-surface px-3 py-2.5 text-sm text-ink focus-visible:border-action focus-visible:outline-none"
          />
        </label>

        {error ? <p className="text-sm font-medium text-slate-700">{error}</p> : null}

        <button
          type="submit"
          disabled={submitting}
          className="rounded-full bg-action px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-action-strong disabled:opacity-50"
        >
          {submitting ? "Entrando…" : "Entrar"}
        </button>
      </form>

      <p className="mt-6 text-sm text-ink-soft">
        Ainda não tem conta?{" "}
        <Link href="/cadastrar" className="font-medium text-action hover:underline">
          Criar conta
        </Link>
      </p>
    </div>
  );
}
