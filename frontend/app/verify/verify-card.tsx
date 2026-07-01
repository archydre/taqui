"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { verifyEmail } from "@/lib/api";

type Status = "prompt" | "verifying" | "success" | "error";

const primaryButton =
  "rounded-full bg-action px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-action-strong";
const secondaryButton =
  "rounded-full border-2 border-line bg-surface px-5 py-2.5 text-sm font-medium text-ink hover:bg-ink/5";

export function VerifyCard() {
  const searchParams = useSearchParams();
  const token = searchParams.get("token");
  const [status, setStatus] = useState<Status>(token ? "verifying" : "prompt");
  const started = useRef(false);

  useEffect(() => {
    // Sem token, o estado inicial já é "prompt" (confira a caixa de entrada).
    // O token é de uso único (o back o apaga ao confirmar); o ref evita a
    // segunda chamada do StrictMode em dev, que cairia em 400.
    if (!token || started.current) return;
    started.current = true;
    verifyEmail(token)
      .then(() => setStatus("success"))
      .catch(() => setStatus((s) => (s === "success" ? s : "error")));
  }, [token]);

  return (
    <div className="w-full max-w-sm animate-scale-in rounded-2xl border border-line bg-surface p-8 text-center shadow-xl">
      <h1 className="font-display text-2xl font-semibold text-ink">
        Verifique seu e-mail
      </h1>

      {status === "prompt" ? (
        <>
          <p className="mt-3 text-sm text-ink-soft">
            Enviamos um link de confirmação para o seu e-mail. Abra a mensagem e
            clique no botão para ativar sua conta.
          </p>
          <div className="mt-6 flex flex-col gap-2">
            <Link href="/entrar" className={primaryButton}>
              Ir para o login
            </Link>
            <Link href="/" className={secondaryButton}>
              Voltar ao início
            </Link>
          </div>
        </>
      ) : null}

      {status === "verifying" ? (
        <>
          <div className="mx-auto mt-6 h-10 w-10 animate-spin rounded-full border-4 border-line border-t-action" />
          <p className="mt-4 text-sm text-ink-soft">Confirmando seu e-mail…</p>
        </>
      ) : null}

      {status === "success" ? (
        <>
          <div className="mx-auto mt-4 flex h-14 w-14 items-center justify-center rounded-full bg-emerald-50">
            <svg
              viewBox="0 0 24 24"
              className="h-8 w-8 text-emerald-600"
              fill="none"
              stroke="currentColor"
              strokeWidth={2.5}
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
            >
              <path d="M20 6 9 17l-5-5" />
            </svg>
          </div>
          <p className="mt-4 text-base font-semibold text-ink">E-mail verificado!</p>
          <p className="mt-1 text-sm text-ink-soft">
            Sua conta está ativa. Bora aproveitar o taqui.
          </p>
          <div className="mt-6 flex flex-col gap-2">
            <Link href="/entrar" className={primaryButton}>
              Entrar
            </Link>
            <Link href="/" className={secondaryButton}>
              Ir para o início
            </Link>
          </div>
        </>
      ) : null}

      {status === "error" ? (
        <>
          <div className="mx-auto mt-4 flex h-14 w-14 items-center justify-center rounded-full bg-ink/5">
            <svg
              viewBox="0 0 24 24"
              className="h-8 w-8 text-ink-soft"
              fill="none"
              stroke="currentColor"
              strokeWidth={2.5}
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
            >
              <path d="M18 6 6 18M6 6l12 12" />
            </svg>
          </div>
          <p className="mt-4 text-base font-semibold text-ink">
            Não foi possível confirmar
          </p>
          <p className="mt-1 text-sm text-ink-soft">
            Esse link pode ter expirado ou já ter sido usado. Tente entrar ou
            gere um novo cadastro.
          </p>
          <div className="mt-6 flex flex-col gap-2">
            <Link href="/entrar" className={primaryButton}>
              Ir para o login
            </Link>
            <Link href="/" className={secondaryButton}>
              Voltar ao início
            </Link>
          </div>
        </>
      ) : null}
    </div>
  );
}
