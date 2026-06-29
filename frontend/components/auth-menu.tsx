"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useAuth } from "@/lib/auth";
import { Avatar } from "./avatar";

export function AuthMenu() {
  const { user, loading, logout } = useAuth();
  const router = useRouter();
  const [open, setOpen] = useState(false);

  if (loading) {
    return <div className="h-9 w-9 animate-pulse rounded-full bg-line" />;
  }

  if (!user) {
    return (
      <div className="flex items-center gap-2">
        <Link
          href="/entrar"
          className="rounded-full px-3 py-2 text-sm font-medium text-ink-soft hover:text-ink"
        >
          Entrar
        </Link>
        <Link
          href="/cadastrar"
          className="rounded-full bg-action px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-action-strong"
        >
          Criar conta
        </Link>
      </div>
    );
  }

  return (
    <div className="relative">
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="flex items-center gap-2 rounded-full p-0.5 hover:bg-ink/5"
        aria-haspopup="menu"
        aria-expanded={open}
      >
        <Avatar name={user.displayName} seed={user.username} size={36} />
      </button>

      {open ? (
        <>
          <button
            type="button"
            className="fixed inset-0 z-30 cursor-default"
            aria-hidden="true"
            tabIndex={-1}
            onClick={() => setOpen(false)}
          />
          <div
            role="menu"
            className="absolute right-0 z-40 mt-2 w-52 overflow-hidden rounded-xl border border-line bg-surface py-1 shadow-lg"
          >
            <div className="border-b border-line px-4 py-3">
              <p className="truncate text-sm font-semibold text-ink">
                {user.displayName}
              </p>
              <p className="truncate text-xs text-ink-soft">@{user.username}</p>
            </div>
            <MenuLink href={`/u/${user.username}`} onClick={() => setOpen(false)}>
              Meu perfil
            </MenuLink>
            <MenuLink href="/vender" onClick={() => setOpen(false)}>
              Vender
            </MenuLink>
            <MenuLink href="/pedidos" onClick={() => setOpen(false)}>
              Meus pedidos
            </MenuLink>
            <MenuLink href="/perfil" onClick={() => setOpen(false)}>
              Editar perfil
            </MenuLink>
            <button
              type="button"
              role="menuitem"
              onClick={() => {
                logout();
                setOpen(false);
                router.push("/");
              }}
              className="block w-full px-4 py-2 text-left text-sm text-ink-soft hover:bg-ink/5 hover:text-ink"
            >
              Sair
            </button>
          </div>
        </>
      ) : null}
    </div>
  );
}

function MenuLink({
  href,
  onClick,
  children,
}: {
  href: string;
  onClick: () => void;
  children: React.ReactNode;
}) {
  return (
    <Link
      href={href}
      role="menuitem"
      onClick={onClick}
      className="block px-4 py-2 text-sm text-ink hover:bg-ink/5"
    >
      {children}
    </Link>
  );
}
