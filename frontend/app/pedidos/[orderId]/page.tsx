"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import { getOrderById, type Order } from "@/lib/api";
import { formatPrice } from "@/lib/format";
import { useAuth } from "@/lib/auth";
import { RequireAuth } from "@/components/require-auth";
import { OrderStatusBadge } from "@/components/order-status-badge";

export default function PedidoPage({
  params,
}: {
  params: Promise<{ orderId: string }>;
}) {
  const { orderId } = use(params);
  return (
    <RequireAuth>
      <PedidoDetail orderId={orderId} />
    </RequireAuth>
  );
}

function PedidoDetail({ orderId }: { orderId: string }) {
  const { token } = useAuth();
  const [order, setOrder] = useState<Order | null>(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!token) return;
    getOrderById(token, orderId)
      .then(setOrder)
      .catch(() => setError(true));
  }, [token, orderId]);

  if (error) {
    return <p className="py-8 text-ink-soft">Não foi possível carregar este pedido.</p>;
  }
  if (!order) {
    return <p className="py-8 text-ink-soft">Carregando…</p>;
  }

  const awaiting = order.status === "AGUARDANDO_PAGAMENTO";

  return (
    <div className="mx-auto w-full max-w-lg py-4">
      <div className="flex items-center justify-between">
        <h1 className="font-display text-2xl font-semibold text-ink">Pedido</h1>
        <OrderStatusBadge status={order.status} />
      </div>

      <div className="mt-6 rounded-2xl border border-line bg-surface p-5">
        <Link
          href={`/produto/${order.product.productId}`}
          className="font-medium text-ink hover:underline"
        >
          {order.product.productName}
        </Link>
        <dl className="mt-4 flex flex-col gap-2 text-sm">
          <Row label="Quantidade" value={`${order.quantity}x`} />
          <Row label="Preço unitário" value={formatPrice(order.unitPrice)} />
          <Row label={`Frete (${order.freightService})`} value={formatPrice(order.freightPrice)} />
          <div className="mt-2 flex items-center justify-between border-t border-line pt-3">
            <dt className="font-medium text-ink">Total</dt>
            <dd className="font-display text-lg font-bold text-ink">
              {formatPrice(order.total)}
            </dd>
          </div>
        </dl>
      </div>

      {awaiting ? (
        <div className="mt-4 rounded-2xl border border-price/30 bg-price/5 p-5">
          <h2 className="font-display font-semibold text-ink">Pague com Pix</h2>
          <p className="mt-1 text-sm text-ink-soft">
            Faça o Pix para a chave abaixo. O vendedor confirma o pagamento e envia.
          </p>
          <p className="mt-3 rounded-lg border border-line bg-surface px-3 py-2 font-mono text-sm break-all text-ink">
            {order.sellerPixKey}
          </p>
        </div>
      ) : null}

      <div className="mt-4 rounded-2xl border border-line bg-surface p-5">
        <h2 className="font-display font-semibold text-ink">Entrega</h2>
        <address className="mt-2 text-sm not-italic text-ink-soft">
          {order.address.recipientName}
          <br />
          {order.address.street}, {order.address.number}
          {order.address.complement ? ` — ${order.address.complement}` : ""}
          <br />
          {order.address.district}, {order.address.city} - {order.address.state}
          <br />
          CEP {order.address.postalCode}
        </address>
      </div>

      <Link
        href="/pedidos"
        className="mt-6 inline-block text-sm text-action hover:underline"
      >
        ← Meus pedidos
      </Link>
    </div>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between">
      <dt className="text-ink-soft">{label}</dt>
      <dd className="text-ink">{value}</dd>
    </div>
  );
}
