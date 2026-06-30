"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ApiError, login as loginRequest, register } from "@/lib/api";
import { useAuth } from "@/lib/auth";

export default function CadastrarPage() {
  const router = useRouter();
  const { login } = useAuth();
  const [form, setForm] = useState({
    displayName: "",
    username: "",
    email: "",
    password: "",
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function update(field: keyof typeof form) {
    return (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm((f) => ({ ...f, [field]: e.target.value }));
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await register(form);
      const { token } = await loginRequest(form.email, form.password);
      await login(token);
      router.push("/");
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : "Não foi possível criar a conta. Tente de novo.",
      );
      setSubmitting(false);
    }
  }

  return (
    <div className="mx-auto w-full max-w-sm py-8">
      <h1 className="font-display text-2xl font-semibold text-ink">Criar conta</h1>
      <p className="mt-1 text-ink-soft">Comece a vender e a comprar no taqui.</p>

      <form onSubmit={onSubmit} className="mt-6 flex flex-col gap-4">
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-ink">Nome</span>
          <input
            required
            maxLength={50}
            value={form.displayName}
            onChange={update("displayName")}
            className="rounded-lg border border-line bg-surface px-3 py-2.5 text-sm text-ink focus-visible:border-action focus-visible:outline-none"
          />
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-ink">Nome de usuário</span>
          <input
            required
            value={form.username}
            onChange={update("username")}
            pattern="[a-z0-9._]{3,30}"
            placeholder="ex: maria.silva"
            className="rounded-lg border border-line bg-surface px-3 py-2.5 text-sm text-ink focus-visible:border-action focus-visible:outline-none"
          />
          <span className="text-xs text-ink-soft">
            3 a 30 caracteres: minúsculas, números, ponto e underscore.
          </span>
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-ink">E-mail</span>
          <input
            type="email"
            required
            value={form.email}
            onChange={update("email")}
            className="rounded-lg border border-line bg-surface px-3 py-2.5 text-sm text-ink focus-visible:border-action focus-visible:outline-none"
          />
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-sm font-medium text-ink">Senha</span>
          <input
            type="password"
            required
            minLength={8}
            maxLength={72}
            value={form.password}
            onChange={update("password")}
            className="rounded-lg border border-line bg-surface px-3 py-2.5 text-sm text-ink focus-visible:border-action focus-visible:outline-none"
          />
          <span className="text-xs text-ink-soft">No mínimo 8 caracteres.</span>
        </label>

        {error ? <p className="text-sm font-medium text-slate-700">{error}</p> : null}

        <button
          type="submit"
          disabled={submitting}
          className="rounded-full bg-action px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-action-strong disabled:opacity-50"
        >
          {submitting ? "Criando…" : "Criar conta"}
        </button>
      </form>

      <p className="mt-6 text-sm text-ink-soft">
        Já tem conta?{" "}
        <Link href="/entrar" className="font-medium text-action hover:underline">
          Entrar
        </Link>
      </p>
    </div>
  );
}
