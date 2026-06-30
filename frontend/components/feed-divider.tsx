"use client";

import { useAuth } from "@/lib/auth";

// Linha que separa a caixa de publicar do feed — só para usuário logado.
export function FeedDivider() {
  const { user } = useAuth();
  if (!user) return null;
  return <div className="mb-4 border-t-2 border-ink/10" aria-hidden="true" />;
}
