"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { getProducts, type Owner } from "@/lib/api";
import { Avatar } from "./avatar";

export function RecommendedUsers({ excludeUsername }: { excludeUsername?: string }) {
  const [users, setUsers] = useState<Owner[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        // Reaproveita o feed público de produtos: cada produto traz o dono.
        const page = await getProducts({ size: 50 });
        if (!active) return;
        const seen = new Set<string>();
        const owners: Owner[] = [];
        for (const product of page.content) {
          const owner = product.owner;
          if (owner.username === excludeUsername) continue;
          if (seen.has(owner.username)) continue;
          seen.add(owner.username);
          owners.push(owner);
        }
        // embaralha (Fisher-Yates) pra variar a recomendação a cada carregamento
        for (let i = owners.length - 1; i > 0; i--) {
          const j = Math.floor(Math.random() * (i + 1));
          [owners[i], owners[j]] = [owners[j], owners[i]];
        }
        setUsers(owners.slice(0, 4));
      } catch {
        // silencioso: some a seção se falhar
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => {
      active = false;
    };
  }, [excludeUsername]);

  if (loading || users.length === 0) return null;

  return (
    <div className="mt-8 animate-fade-in">
      <p className="mb-3 text-sm font-semibold text-ink">Recomendações para você</p>
      <ul className="flex flex-col gap-3">
        {users.map((u) => (
          <li key={u.username}>
            <Link href={`/u/${u.username}`} className="flex items-center gap-3">
              <Avatar name={u.displayName} seed={u.username} size={48} />
              <div className="min-w-0 flex-1">
                <p className="truncate font-semibold text-ink hover:underline">
                  {u.username}
                </p>
                <p className="truncate text-sm text-ink-soft">{u.displayName}</p>
              </div>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
