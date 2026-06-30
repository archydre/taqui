"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/lib/auth";

const VISITS_KEY = "taqui_welcome_visits";
const OFF_KEY = "taqui_welcome_off";
const authRoutes = ["/entrar", "/cadastrar"];

// Lê o contador e decide se mostra (1ª, 4ª, 7ª… = uma a cada 3). Só leitura.
function decide(): { show: boolean; nextVisits: number } {
  if (typeof window === "undefined") return { show: false, nextVisits: 0 };
  try {
    if (window.localStorage.getItem(OFF_KEY) === "1") {
      return { show: false, nextVisits: 0 };
    }
    const visits = Number(window.localStorage.getItem(VISITS_KEY) ?? "0") + 1;
    return { show: visits % 3 === 1, nextVisits: visits };
  } catch {
    return { show: false, nextVisits: 0 };
  }
}

export function WelcomeModal() {
  const { user, loading } = useAuth();
  const pathname = usePathname();

  if (loading || user || authRoutes.includes(pathname)) return null;
  return <WelcomeDialog />;
}

function WelcomeDialog() {
  const [info] = useState(decide);
  const [dismissed, setDismissed] = useState(false);

  useEffect(() => {
    if (info.nextVisits > 0) {
      try {
        window.localStorage.setItem(VISITS_KEY, String(info.nextVisits));
      } catch {
        // localStorage indisponível: ignora
      }
    }
  }, [info]);

  if (!info.show || dismissed) return null;

  function close() {
    setDismissed(true);
  }

  function dontShowAgain() {
    try {
      window.localStorage.setItem(OFF_KEY, "1");
    } catch {
      // ignora
    }
    setDismissed(true);
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink p-4">
      <div
        role="dialog"
        aria-modal="true"
        className="relative w-full max-w-sm rounded-2xl border border-line bg-surface p-8 text-center shadow-xl"
      >
        <button
          type="button"
          onClick={close}
          aria-label="Fechar"
          className="absolute right-3 top-3 grid h-8 w-8 place-items-center rounded-full text-ink-soft hover:bg-ink/5"
        >
          ✕
        </button>

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
            className="rounded-full bg-action px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-action-strong"
          >
            Criar conta
          </Link>
          <Link
            href="/entrar"
            className="rounded-full border border-line bg-surface px-5 py-2.5 text-sm font-medium text-ink hover:bg-ink/5"
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

        <button
          type="button"
          onClick={dontShowAgain}
          className="mt-4 text-xs text-ink-soft underline hover:text-ink"
        >
          Não mostrar novamente
        </button>
      </div>
    </div>
  );
}
