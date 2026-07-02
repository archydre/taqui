"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import { getCart } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { IconCart } from "./icons";

const POLL_MS = 30_000;

// Carrinho no side-rail com badge de itens. O link aparece sempre (deslogado, /carrinho pede
// login); a contagem só é buscada com token. Atualiza por polling, ao focar a aba e ao trocar de rota.
export function CartLink() {
  const { token } = useAuth();
  const pathname = usePathname();
  const [count, setCount] = useState(0);

  const refresh = useCallback(() => {
    if (!token) return;
    getCart(token)
      .then((items) => setCount(items.length))
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

  useEffect(() => {
    refresh();
  }, [pathname, refresh]);

  const active = pathname.startsWith("/carrinho");
  const label = count > 0 ? `Carrinho (${count})` : "Carrinho";
  const badge =
    token && count > 0 ? (
      <span
        className="absolute -right-0.5 -top-0.5 grid min-w-[18px] place-items-center rounded-full bg-action px-1 text-[11px] font-semibold leading-[18px] text-white"
        aria-label={`${count} no carrinho`}
      >
        {count > 9 ? "9+" : count}
      </span>
    ) : null;

  return (
    <Link
      href="/carrinho"
      title={label}
      aria-label={label}
      aria-current={active ? "page" : undefined}
      className={`relative grid h-12 w-12 place-items-center rounded-xl transition-colors ${
        active ? "bg-ink text-white" : "text-ink hover:bg-ink/5"
      }`}
    >
      <IconCart className="h-6 w-6" />
      {badge}
    </Link>
  );
}
