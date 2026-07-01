import type { Metadata } from "next";
import { Suspense } from "react";
import { VerifyCard } from "./verify-card";

export const metadata: Metadata = {
  title: "Verifique seu e-mail · taqui",
};

// useSearchParams (dentro do VerifyCard) exige um limite de Suspense, senão o
// build de produção falha ("Missing Suspense boundary with useSearchParams").
export default function VerifyPage() {
  return (
    <div className="flex min-h-[70vh] items-center justify-center px-4">
      <Suspense fallback={<VerifyCardFallback />}>
        <VerifyCard />
      </Suspense>
    </div>
  );
}

function VerifyCardFallback() {
  return (
    <div className="w-full max-w-sm rounded-2xl border border-line bg-surface p-8 text-center shadow-xl">
      <h1 className="font-display text-2xl font-semibold text-ink">
        Verifique seu e-mail
      </h1>
      <div className="mx-auto mt-6 h-10 w-10 animate-spin rounded-full border-4 border-line border-t-action" />
    </div>
  );
}
