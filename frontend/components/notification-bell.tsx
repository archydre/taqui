"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import { getUnreadNotificationCount } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { IconBell } from "./icons";

const POLL_MS = 30_000;

// Sino com badge de não-lidas. Só aparece logado. Atualiza por polling (MVP) e ao focar a aba.
export function NotificationBell({ variant = "rail" }: { variant?: "rail" | "bar" }) {
  const { token } = useAuth();
  const pathname = usePathname();
  const [count, setCount] = useState(0);

  const refresh = useCallback(() => {
    if (!token) return;
    getUnreadNotificationCount(token)
      .then((r) => setCount(r.count))
      .catch(() => {});
  }, [token]);

  useEffect(() => {
    if (!token) return;
    const id = window.setInterval(refresh, POLL_MS);
    const onFocus = () => refresh();
    window.addEventListener("focus", onFocus);
    return () => {
      window.clearInterval(id);
      window.removeEventListener("focus", onFocus);
    };
  }, [token, refresh]);

  // Refetch ao trocar de rota: serve de carga inicial e atualiza ao sair de /notificacoes.
  useEffect(() => {
    refresh();
  }, [pathname, refresh]);

  if (!token) return null;

  const active = pathname.startsWith("/notificacoes");
  // Na própria página de notificações o badge some (o usuário está lendo).
  const badgeCount = active ? 0 : count;
  const badge =
    badgeCount > 0 ? (
      <span
        className="absolute -right-0.5 -top-0.5 grid min-w-[18px] place-items-center rounded-full bg-action px-1 text-[11px] font-semibold leading-[18px] text-white"
        aria-label={`${badgeCount} não lidas`}
      >
        {badgeCount > 9 ? "9+" : badgeCount}
      </span>
    ) : null;

  const label = badgeCount > 0 ? `Notificações (${badgeCount} não lidas)` : "Notificações";

  if (variant === "bar") {
    return (
      <Link
        href="/notificacoes"
        title={label}
        aria-label={label}
        className="relative grid h-10 w-10 place-items-center rounded-full text-ink hover:bg-ink/5"
      >
        <IconBell className="h-6 w-6" />
        {badge}
      </Link>
    );
  }

  return (
    <Link
      href="/notificacoes"
      title={label}
      aria-label={label}
      aria-current={active ? "page" : undefined}
      className={`relative grid h-12 w-12 place-items-center rounded-xl transition-colors ${
        active ? "bg-ink text-white" : "text-ink hover:bg-ink/5"
      }`}
    >
      <IconBell className="h-6 w-6" />
      {badge}
    </Link>
  );
}
