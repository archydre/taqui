"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";

export function RequireAuth({ children }: { children: React.ReactNode }) {
  const { token, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && !token) router.replace("/entrar");
  }, [loading, token, router]);

  if (loading || !token) {
    return <AuthSkeleton />;
  }
  return <>{children}</>;
}

function AuthSkeleton() {
  return (
    <div className="mx-auto w-full max-w-md py-8" aria-hidden="true">
      <div className="h-7 w-44 animate-pulse rounded bg-line" />
      <div className="mt-2 h-4 w-64 animate-pulse rounded bg-line" />

      <div className="mt-6 flex flex-col gap-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="flex flex-col gap-2">
            <div className="h-3 w-24 animate-pulse rounded bg-line" />
            <div className="h-10 w-full animate-pulse rounded-lg bg-line" />
          </div>
        ))}
        <div className="mt-1 h-11 w-full animate-pulse rounded-full bg-line" />
      </div>
    </div>
  );
}
