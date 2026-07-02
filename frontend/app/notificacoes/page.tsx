"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  getNotifications,
  markAllNotificationsRead,
  markNotificationRead,
  notificationHref,
  type Notification,
} from "@/lib/api";
import { formatRelativeTime } from "@/lib/format";
import { useAuth } from "@/lib/auth";
import { RequireAuth } from "@/components/require-auth";

export default function NotificacoesPage() {
  return (
    <RequireAuth>
      <NotificacoesList />
    </RequireAuth>
  );
}

function NotificacoesList() {
  const { token } = useAuth();
  const router = useRouter();
  const [items, setItems] = useState<Notification[] | null>(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!token) return;
    getNotifications(token)
      .then((page) => setItems(page.content))
      .catch(() => setError(true));
  }, [token]);

  const open = async (n: Notification) => {
    if (token && !n.read) {
      try {
        await markNotificationRead(token, n.id);
      } catch {
        // segue pro recurso mesmo se o mark falhar
      }
    }
    router.push(notificationHref(n));
  };

  const markAll = async () => {
    if (!token) return;
    try {
      await markAllNotificationsRead(token);
      setItems((prev) => prev?.map((n) => ({ ...n, read: true })) ?? prev);
    } catch {
      // silencioso; o usuário pode tentar de novo
    }
  };

  if (error) {
    return <p className="py-8 text-ink-soft">Não foi possível carregar suas notificações.</p>;
  }
  if (items === null) {
    return <p className="py-8 text-ink-soft">Carregando…</p>;
  }

  const hasUnread = items.some((n) => !n.read);

  return (
    <div className="mx-auto w-full max-w-2xl py-4">
      <div className="flex items-center justify-between gap-4">
        <h1 className="font-display text-2xl font-semibold text-ink">Notificações</h1>
        {hasUnread ? (
          <button
            type="button"
            onClick={markAll}
            className="text-sm font-medium text-action hover:underline"
          >
            Marcar todas como lidas
          </button>
        ) : null}
      </div>

      {items.length === 0 ? (
        <p className="mt-4 text-ink-soft">Você ainda não tem notificações.</p>
      ) : (
        <ul className="mt-6 flex flex-col gap-2">
          {items.map((n) => (
            <li key={n.id}>
              <button
                type="button"
                onClick={() => open(n)}
                className={`flex w-full items-start gap-3 rounded-xl border border-line p-4 text-left transition-colors hover:bg-ink/5 ${
                  n.read ? "bg-surface" : "bg-action/5"
                }`}
              >
                <span
                  className={`mt-1.5 h-2 w-2 shrink-0 rounded-full ${
                    n.read ? "bg-transparent" : "bg-action"
                  }`}
                  aria-hidden="true"
                />
                <span className="min-w-0 flex-1">
                  <span className="block text-ink">{n.message}</span>
                  <span className="mt-1 block text-xs text-ink-soft">
                    {formatRelativeTime(n.createdAt)}
                  </span>
                </span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
