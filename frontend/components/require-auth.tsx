"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";

export function RequireAuth({ children }: { children: React.ReactNode }) {
  const { token, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && !token) router.replace("/entrar");
  }, [loading, token, router]);

  if (loading) {
    return <p className="text-ink-soft">Carregando…</p>;
  }
  if (!token) {
    return null;
  }
  return <>{children}</>;
}
