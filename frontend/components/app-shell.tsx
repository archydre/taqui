"use client";

import { usePathname } from "next/navigation";
import { TopBar } from "./top-bar";
import { SideRail } from "./side-rail";
import { ProfileSidebar } from "./profile-sidebar";

const authRoutes = ["/entrar", "/cadastrar"];

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const isAuth = authRoutes.includes(pathname);

  return (
    <div className="min-h-full">
      <div className="lg:hidden">
        <TopBar />
      </div>

      <div className="mx-auto flex w-full max-w-[1100px] gap-6 px-4">
        <aside className="sticky top-0 hidden h-screen w-[76px] shrink-0 border-r border-line lg:block">
          <SideRail />
        </aside>

        <main className="min-w-0 flex-1 py-6">{children}</main>

        {!isAuth ? (
          <aside className="sticky top-0 hidden h-screen w-[300px] shrink-0 lg:block">
            <ProfileSidebar />
          </aside>
        ) : null}
      </div>
    </div>
  );
}
