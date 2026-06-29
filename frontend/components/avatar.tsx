const palette = [
  "#f26b1d",
  "#1b8a5a",
  "#3b6fd4",
  "#b5468c",
  "#c9a227",
  "#5a6b7b",
];

function initials(name: string): string {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  const first = parts[0]?.[0] ?? "";
  const last = parts.length > 1 ? parts[parts.length - 1][0] : "";
  return (first + last).toUpperCase() || "?";
}

function colorFor(seed: string): string {
  let hash = 0;
  for (let i = 0; i < seed.length; i++) {
    hash = (hash * 31 + seed.charCodeAt(i)) >>> 0;
  }
  return palette[hash % palette.length];
}

export function Avatar({
  name,
  seed,
  size = 40,
}: {
  name: string;
  seed?: string;
  size?: number;
}) {
  return (
    <span
      className="inline-grid shrink-0 place-items-center rounded-full font-display font-semibold text-white"
      style={{
        width: size,
        height: size,
        backgroundColor: colorFor(seed ?? name),
        fontSize: size * 0.4,
      }}
      aria-hidden="true"
    >
      {initials(name)}
    </span>
  );
}
