"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const tabs = [
  { href: "/", label: "Feed" },
  { href: "/explorar", label: "Explorar" },
];

export function MainNav() {
  const pathname = usePathname();

  return (
    <nav className="flex gap-1">
      {tabs.map((tab) => {
        const active = pathname === tab.href;
        return (
          <Link
            key={tab.href}
            href={tab.href}
            aria-current={active ? "page" : undefined}
            className={`rounded-full px-4 py-2 text-sm font-medium transition-colors ${
              active
                ? "bg-ink text-white"
                : "text-ink-soft hover:bg-ink/5 hover:text-ink"
            }`}
          >
            {tab.label}
          </Link>
        );
      })}
    </nav>
  );
}
