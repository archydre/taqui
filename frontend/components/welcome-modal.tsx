"use client";

import { useState, useSyncExternalStore } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/lib/auth";

const authRoutes = ["/entrar", "/cadastrar", "/verify"];

// Quem decide se mostra é o script inline do layout (roda antes da pintura e
// marca html.welcome-open). Aqui só lemos essa classe, sem flash de hidratação.
function subscribe() {
  return () => {};
}
function isOpen() {
  return document.documentElement.classList.contains("welcome-open");
}

export function WelcomeModal({ onReveal }: { onReveal?: () => void }) {
  const { user } = useAuth();
  const pathname = usePathname();
  const open = useSyncExternalStore(subscribe, isOpen, () => false);
  const [dismissed, setDismissed] = useState(false);

  if (user || authRoutes.includes(pathname) || !open || dismissed) return null;

  // Sair (Entrar / Criar conta / Continuar) fecha o modal nesta navegação;
  // deslogado, ele volta a aparecer no próximo carregamento.
  function leave() {
    document.documentElement.classList.remove("welcome-open");
  }

  function close() {
    leave();
    onReveal?.();
    setDismissed(true);
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink p-4">
      <div
        role="dialog"
        aria-modal="true"
        className="relative w-full max-w-sm animate-scale-in rounded-2xl border border-line bg-surface p-8 text-center shadow-xl"
      >
        <div className="flex items-center justify-center gap-3">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src="/taqui_logo.png"
            alt=""
            width={80}
            height={80}
            className="h-20 w-20 object-cover"
          />
          <span className="font-display text-4xl font-bold tracking-tight text-ink">
            TAQUI
          </span>
        </div>

        <p className="mt-3 text-sm tracking-wide text-ink-soft">
          tudo o que você procura{" "}
          <span className="font-bold text-ink">TAQUI</span>
        </p>

        <div className="mt-6 flex flex-col gap-2">
          <Link
            href="/cadastrar"
            onClick={leave}
            className="rounded-full bg-action px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-action-strong"
          >
            Criar conta
          </Link>
          <Link
            href="/entrar"
            onClick={leave}
            className="rounded-full border-2 border-line bg-surface px-5 py-2.5 text-sm font-medium text-ink hover:bg-ink/5"
          >
            Entrar
          </Link>
          <button
            type="button"
            onClick={close}
            className="mt-1 text-sm font-medium text-ink-soft hover:text-ink"
          >
            Continuar sem conta
          </button>
        </div>
      </div>
    </div>
  );
}
