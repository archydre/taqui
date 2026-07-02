"use client";

import { useEffect, useState } from "react";
import { QRCodeSVG } from "qrcode.react";
import { getPixQr } from "@/lib/api";
import { useAuth } from "@/lib/auth";

type Method = "key" | "qr";

export function PixPayment({ orderId, pixKey }: { orderId: string; pixKey: string }) {
  const { token } = useAuth();
  const [method, setMethod] = useState<Method>("key");
  const [copyPaste, setCopyPaste] = useState<string | null>(null);
  const [error, setError] = useState(false);
  const [copied, setCopied] = useState(false);

  // Busca o "copia e cola" só quando a aba QR é aberta (uma vez).
  useEffect(() => {
    if (method !== "qr" || copyPaste || !token) return;
    getPixQr(token, orderId)
      .then((res) => setCopyPaste(res.copyPaste))
      .catch(() => setError(true));
  }, [method, copyPaste, token, orderId]);

  async function copy() {
    if (!copyPaste) return;
    try {
      await navigator.clipboard.writeText(copyPaste);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // clipboard indisponível — ignora
    }
  }

  return (
    <div className="mt-4 rounded-2xl border border-price/30 bg-price/5 p-5">
      <h2 className="font-display font-semibold text-ink">Pague com Pix</h2>

      <div className="mt-3 inline-flex rounded-full border border-line bg-surface p-0.5 text-sm">
        {(["key", "qr"] as const).map((m) => (
          <button
            key={m}
            type="button"
            onClick={() => setMethod(m)}
            className={`rounded-full px-4 py-1.5 font-medium transition-colors ${
              method === m ? "bg-price text-white" : "text-ink-soft hover:text-ink"
            }`}
          >
            {m === "key" ? "Chave Pix" : "QR Code"}
          </button>
        ))}
      </div>

      {method === "key" ? (
        <>
          <p className="mt-3 text-sm text-ink-soft">
            Faça o Pix para a chave abaixo. O vendedor confirma o pagamento e envia.
          </p>
          <p className="mt-3 rounded-lg border border-line bg-surface px-3 py-2 font-mono text-sm break-all text-ink">
            {pixKey}
          </p>
        </>
      ) : (
        <div className="mt-3 flex flex-col items-center gap-3">
          <p className="text-center text-sm text-ink-soft">
            Escaneie no app do seu banco, ou copie o código.
          </p>
          {error ? (
            <p className="text-sm font-medium text-slate-700">
              Não deu para gerar o QR agora.
            </p>
          ) : copyPaste ? (
            <>
              <div className="rounded-lg bg-white p-3 shadow-sm">
                <QRCodeSVG value={copyPaste} size={200} />
              </div>
              <button
                type="button"
                onClick={copy}
                className="rounded-full bg-price px-4 py-2 text-sm font-semibold text-white transition-colors hover:opacity-90"
              >
                {copied ? "Copiado! ✓" : "Copiar código Pix"}
              </button>
            </>
          ) : (
            <div className="h-[224px] w-[224px] animate-pulse rounded-lg bg-line" />
          )}
        </div>
      )}
    </div>
  );
}
