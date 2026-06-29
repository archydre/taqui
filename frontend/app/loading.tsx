export default function Loading() {
  return (
    <div className="mx-auto flex w-full max-w-xl flex-col gap-4">
      {Array.from({ length: 4 }).map((_, i) => (
        <div
          key={i}
          className="rounded-2xl border border-line bg-surface p-5 shadow-sm"
        >
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 animate-pulse rounded-full bg-line" />
            <div className="flex flex-col gap-2">
              <div className="h-3 w-32 animate-pulse rounded bg-line" />
              <div className="h-3 w-20 animate-pulse rounded bg-line" />
            </div>
          </div>
          <div className="mt-4 h-3 w-full animate-pulse rounded bg-line" />
          <div className="mt-2 h-3 w-2/3 animate-pulse rounded bg-line" />
          <div className="mt-4 aspect-video w-full animate-pulse rounded-xl bg-line" />
        </div>
      ))}
    </div>
  );
}
