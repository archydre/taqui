import { Brand } from "./brand";
import { SearchBar } from "./search-bar";
import { MainNav } from "./main-nav";
import { AuthMenu } from "./auth-menu";
import { NotificationBell } from "./notification-bell";

export function TopBar() {
  return (
    <header className="sticky top-0 z-20 border-b border-line bg-surface/80 backdrop-blur">
      <div className="mx-auto flex w-full max-w-6xl items-center gap-4 px-4 py-3">
        <Brand />
        <div className="hidden flex-1 justify-center sm:flex">
          <div className="w-full max-w-md">
            <SearchBar />
          </div>
        </div>
        <div className="ml-auto flex items-center gap-2 sm:ml-0">
          <MainNav />
          <NotificationBell variant="bar" />
          <AuthMenu />
        </div>
      </div>
      <div className="border-t border-line px-4 py-2 sm:hidden">
        <SearchBar />
      </div>
    </header>
  );
}
