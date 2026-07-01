const priceFormatter = new Intl.NumberFormat("pt-BR", {
  style: "currency",
  currency: "BRL",
});

export function formatPrice(price: number): string {
  return priceFormatter.format(price);
}

const relativeFormatter = new Intl.RelativeTimeFormat("pt-BR", {
  numeric: "auto",
});

export function formatRelativeTime(iso: string): string {
  const seconds = Math.round((new Date(iso).getTime() - Date.now()) / 1000);
  const abs = Math.abs(seconds);

  if (abs < 60) return relativeFormatter.format(seconds, "second");
  const minutes = Math.round(seconds / 60);
  if (Math.abs(minutes) < 60) return relativeFormatter.format(minutes, "minute");
  const hours = Math.round(minutes / 60);
  if (Math.abs(hours) < 24) return relativeFormatter.format(hours, "hour");
  const days = Math.round(hours / 24);
  if (Math.abs(days) < 30) return relativeFormatter.format(days, "day");
  const months = Math.round(days / 30);
  if (Math.abs(months) < 12) return relativeFormatter.format(months, "month");
  const years = Math.round(months / 12);
  return relativeFormatter.format(years, "year");
}
