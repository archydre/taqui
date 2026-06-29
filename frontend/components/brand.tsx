import Link from "next/link";

export function Brand() {
  return (
    <Link href="/" className="flex shrink-0 items-center gap-2">
      <span className="grid h-9 w-9 place-items-center rounded-xl bg-ink font-display text-lg font-bold text-white">
        t
      </span>
      <span className="font-display text-xl font-semibold tracking-tight text-ink">
        taqui
      </span>
    </Link>
  );
}
