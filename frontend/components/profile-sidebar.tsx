"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { Avatar } from "./avatar";
import { SearchBar } from "./search-bar";

export function ProfileSidebar() {
  const { user, loading, logout } = useAuth();
  const router = useRouter();

  return (
    <div className="flex flex-col gap-8 py-6">
      <SearchBar />

      <div className="flex flex-1 items-center">
        <div className="w-full">
          {loading ? (
            <div className="animate-pulse rounded-2xl border border-line bg-surface p-4">
              <div className="mx-auto h-4 w-3/4 rounded bg-line" />
              <div className="mt-4 flex flex-col gap-2">
                <div className="h-9 rounded-full bg-line" />
                <div className="h-9 rounded-full bg-line" />
              </div>
            </div>
          ) : user ? (
            <div className="flex items-center gap-3">
              <Link href={`/u/${user.username}`} className="shrink-0">
                <Avatar name={user.displayName} seed={user.username} size={48} />
              </Link>
              <div className="min-w-0 flex-1">
                <Link
                  href={`/u/${user.username}`}
                  className="block truncate font-semibold text-ink hover:underline"
                >
                  {user.username}
                </Link>
                <p className="truncate text-sm text-ink-soft">{user.displayName}</p>
              </div>
              <button
                type="button"
                onClick={() => {
                  logout();
                  router.push("/");
                }}
                className="text-sm font-semibold text-action hover:underline"
              >
                Sair
              </button>
            </div>
          ) : (
            <div className="rounded-2xl border border-line bg-surface p-4 text-center">
              <p className="text-sm text-ink-soft">Entre para publicar e vender.</p>
              <div className="mt-3 flex flex-col gap-2">
                <Link
                  href="/entrar"
                  className="rounded-full bg-action px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-action-strong"
                >
                  Entrar
                </Link>
                <Link
                  href="/cadastrar"
                  className="rounded-full border border-line px-4 py-2 text-sm font-medium text-ink hover:bg-ink/5"
                >
                  Criar conta
                </Link>
              </div>
            </div>
          )}
        </div>
      </div>

      <footer className="text-xs text-ink-soft">© 2026 taqui</footer>
    </div>
  );
}
