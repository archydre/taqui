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

      <aside className="fixed top-0 left-0 z-20 hidden h-screen w-[64px] border-r border-line bg-canvas lg:block">
        <SideRail />
      </aside>

      <div className="lg:pl-[64px]">
        <div className="mx-auto flex w-full max-w-[1000px] gap-6 px-4">
          <main className="min-w-0 flex-1 py-6">{children}</main>

          {!isAuth ? (
            <aside className="sticky top-0 hidden h-screen w-[300px] shrink-0 lg:block">
              <ProfileSidebar />
            </aside>
          ) : null}
        </div>
      </div>
    </div>
  );
}
