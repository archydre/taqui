"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/lib/auth";
import {
  IconBag,
  IconGrid,
  IconHome,
  IconPlus,
  IconUser,
} from "./icons";

export function SideRail() {
  const pathname = usePathname();
  const { user } = useAuth();

  const items = [
    { label: "Feed", href: "/", icon: IconHome, active: pathname === "/" },
    {
      label: "Explorar",
      href: "/explorar",
      icon: IconGrid,
      active: pathname.startsWith("/explorar") || pathname.startsWith("/produto"),
    },
    { label: "Vender", href: "/vender", icon: IconPlus, active: pathname.startsWith("/vender") },
    { label: "Pedidos", href: "/pedidos", icon: IconBag, active: pathname.startsWith("/pedidos") },
    {
      label: "Perfil",
      href: user ? `/u/${user.username}` : "/entrar",
      icon: IconUser,
      active: pathname.startsWith("/u/") || pathname.startsWith("/perfil"),
    },
  ];

  return (
    <nav className="flex h-full flex-col items-center gap-1 py-4">
      <Link
        href="/"
        title="taqui"
        aria-label="taqui — início"
        className="mb-4 grid h-10 w-10 place-items-center rounded-xl bg-ink font-display text-lg font-bold text-white"
      >
        t
      </Link>

      {items.map(({ label, href, icon: Icon, active }) => (
        <Link
          key={label}
          href={href}
          title={label}
          aria-label={label}
          aria-current={active ? "page" : undefined}
          className={`grid h-12 w-12 place-items-center rounded-xl transition-colors ${
            active ? "bg-ink text-white" : "text-ink hover:bg-ink/5"
          }`}
        >
          <Icon className="h-6 w-6" />
        </Link>
      ))}
    </nav>
  );
}
