import Link from "next/link";

export function Brand() {
  return (
    <Link href="/" className="flex shrink-0 items-center gap-2">
      {/* eslint-disable-next-line @next/next/no-img-element */}
      <img
        src="/taqui_logo.jpg"
        alt="taqui"
        width={36}
        height={36}
        className="h-9 w-9 rounded-xl object-cover"
      />
      <span className="font-display text-xl font-semibold tracking-tight text-ink">
        taqui
      </span>
    </Link>
  );
}
