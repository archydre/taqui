export function SearchBar() {
  return (
    <form action="/explorar" method="get" role="search" className="relative w-full">
      <input
        type="search"
        name="q"
        placeholder="Buscar produtos…"
        aria-label="Buscar produtos"
        className="w-full rounded-full border border-line bg-canvas px-5 py-2.5 text-sm text-ink placeholder:text-ink-soft focus-visible:border-action focus-visible:outline-none"
      />
    </form>
  );
}
