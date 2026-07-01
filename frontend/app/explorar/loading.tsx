export default function Loading() {
  return (
    <div>
      <div className="mb-6">
        <div className="h-7 w-40 animate-pulse rounded bg-line" />
        <div className="mt-2 h-4 w-64 animate-pulse rounded bg-line" />
      </div>
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
        {Array.from({ length: 8 }).map((_, i) => (
          <div
            key={i}
            className="flex flex-col overflow-hidden rounded-2xl border border-line bg-surface"
          >
            <div className="aspect-square animate-pulse bg-line" />
            <div className="flex flex-col gap-2 p-4">
              <div className="h-4 w-3/4 animate-pulse rounded bg-line" />
              <div className="h-3 w-1/2 animate-pulse rounded bg-line" />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
