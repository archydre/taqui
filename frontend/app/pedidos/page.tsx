"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { getMyOrders, type Order } from "@/lib/api";
import { formatPrice } from "@/lib/format";
import { useAuth } from "@/lib/auth";
import { RequireAuth } from "@/components/require-auth";
import { OrderStatusBadge } from "@/components/order-status-badge";

export default function PedidosPage() {
  return (
    <RequireAuth>
      <PedidosList />
    </RequireAuth>
  );
}

function PedidosList() {
  const { token } = useAuth();
  const [orders, setOrders] = useState<Order[] | null>(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!token) return;
    getMyOrders(token)
      .then((page) => setOrders(page.content))
      .catch(() => setError(true));
  }, [token]);

  if (error) {
    return <p className="py-8 text-ink-soft">Não foi possível carregar seus pedidos.</p>;
  }
  if (orders === null) {
    return <p className="py-8 text-ink-soft">Carregando…</p>;
  }

  return (
    <div className="mx-auto w-full max-w-2xl py-4">
      <h1 className="font-display text-2xl font-semibold text-ink">Meus pedidos</h1>

      {orders.length === 0 ? (
        <p className="mt-4 text-ink-soft">
          Você ainda não fez nenhum pedido.{" "}
          <Link href="/explorar" className="font-medium text-action hover:underline">
            Explorar produtos
          </Link>
        </p>
      ) : (
        <ul className="mt-6 flex flex-col gap-3">
          {orders.map((order) => (
            <li key={order.orderId}>
              <Link
                href={`/pedidos/${order.orderId}`}
                className="flex items-center justify-between rounded-xl border border-line bg-surface p-4 hover:bg-ink/5"
              >
                <div className="min-w-0">
                  <p className="truncate font-medium text-ink">
                    {order.product.productName}
                  </p>
                  <p className="mt-1 text-sm text-ink-soft">
                    {formatPrice(order.total)} · {order.quantity}x
                  </p>
                </div>
                <OrderStatusBadge status={order.status} />
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
