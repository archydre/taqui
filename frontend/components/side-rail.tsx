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
import type { IconProps } from "./icons";
import { CartLink } from "./cart-link";
import { NotificationBell } from "./notification-bell";
import type { ComponentType } from "react";

type RailItem = {
  label: string;
  href: string;
  icon: ComponentType<IconProps>;
  active: boolean;
};

export function SideRail() {
  const pathname = usePathname();
  const { user } = useAuth();

  const top: RailItem[] = [
    { label: "Feed", href: "/", icon: IconHome, active: pathname === "/" },
    {
      label: "Explorar",
      href: "/explorar",
      icon: IconGrid,
      active: pathname.startsWith("/explorar") || pathname.startsWith("/produto"),
    },
    { label: "Vender", href: "/vender", icon: IconPlus, active: pathname.startsWith("/vender") },
  ];

  const bottom: RailItem[] = [
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
        className="mb-4 block"
      >
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img
          src="/taqui_logo.png"
          alt="taqui"
          width={40}
          height={40}
          className="h-10 w-10 rounded-xl object-cover"
        />
      </Link>

      {top.map((item) => (
        <RailLink key={item.label} {...item} />
      ))}

      <CartLink />

      {bottom.map((item) => (
        <RailLink key={item.label} {...item} />
      ))}

      <NotificationBell />
    </nav>
  );
}

function RailLink({ label, href, icon: Icon, active }: RailItem) {
  return (
    <Link
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
  );
}
