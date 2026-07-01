import { type OrderStatus } from "@/lib/api";

const labels: Record<OrderStatus, { text: string; className: string }> = {
  AGUARDANDO_PAGAMENTO: {
    text: "Aguardando pagamento",
    className: "bg-indigo-100 text-indigo-800",
  },
  PAGO: { text: "Pago", className: "bg-price/10 text-price" },
  ENVIADO: { text: "Enviado", className: "bg-blue-100 text-blue-800" },
  CANCELADO: { text: "Cancelado", className: "bg-ink/10 text-ink-soft" },
};

export function OrderStatusBadge({ status }: { status: OrderStatus }) {
  const info = labels[status] ?? {
    text: status,
    className: "bg-ink/10 text-ink-soft",
  };
  return (
    <span
      className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium ${info.className}`}
    >
      {info.text}
    </span>
  );
}
