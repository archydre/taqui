"use client";

import { usePathname } from "next/navigation";
import { TopBar } from "./top-bar";
import { SideRail } from "./side-rail";
import { ProfileSidebar } from "./profile-sidebar";
import { WelcomeModal } from "./welcome-modal";

const authRoutes = ["/entrar", "/cadastrar"];

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const isAuth = authRoutes.includes(pathname);

  return (
    <div className="min-h-full">
      <div className="lg:hidden">
        <TopBar />
      </div>

      <aside className="fixed top-0 left-0 z-20 hidden h-screen w-[72px] border-r border-line bg-canvas lg:block">
        <SideRail />
      </aside>

      <div className="lg:pl-[72px] lg:pr-20">
        <div className="mx-auto flex w-full max-w-[1000px] gap-16 px-4 pt-4">
          <main className="min-w-0 flex-1 py-6">{children}</main>

          {!isAuth ? (
            <aside className="sticky top-0 hidden h-screen w-[300px] shrink-0 lg:block">
              <ProfileSidebar />
            </aside>
          ) : null}
        </div>
      </div>

      <WelcomeModal />
    </div>
  );
}
