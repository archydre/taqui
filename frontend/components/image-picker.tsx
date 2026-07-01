"use client";

import { useEffect, useRef, useState } from "react";
import { ApiError, uploadImage, type UploadResult } from "@/lib/api";

const ACCEPT = "image/png,image/jpeg,image/webp";
const ALLOWED = ["image/png", "image/jpeg", "image/webp"];
const MAX_BYTES = 5 * 1024 * 1024;

type Props = {
  token: string | null;
  imageUrl: string | null;
  onUploaded: (urls: UploadResult) => void;
  onClear: () => void;
  onUploadingChange?: (uploading: boolean) => void;
  disabled?: boolean;
};

export function ImagePicker({
  token,
  imageUrl,
  onUploaded,
  onClear,
  onUploadingChange,
  disabled,
}: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [preview, setPreview] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Revoga o object URL ao trocar de preview e ao desmontar.
  useEffect(() => {
    return () => {
      if (preview) URL.revokeObjectURL(preview);
    };
  }, [preview]);

  async function onPick(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    e.target.value = "";
    if (!file) return;
    if (!token) {
      setError("Entre para enviar imagens.");
      return;
    }
    if (!ALLOWED.includes(file.type)) {
      setError("Formato inválido. Use PNG, JPEG ou WebP.");
      return;
    }
    if (file.size > MAX_BYTES) {
      setError("Imagem muito grande (máximo 5 MB).");
      return;
    }
    setPreview(URL.createObjectURL(file));
    setError(null);
    setUploading(true);
    onUploadingChange?.(true);
    try {
      onUploaded(await uploadImage(token, file));
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Não deu para enviar a imagem.",
      );
      setPreview(null);
      onClear();
    } finally {
      setUploading(false);
      onUploadingChange?.(false);
    }
  }

  function clear() {
    setPreview(null);
    setError(null);
    onClear();
  }

  const shown = preview ?? imageUrl;

  return (
    <div>
      {shown ? (
        <div className="relative inline-block">
          <img
            src={shown}
            alt="Prévia da imagem"
            className="max-h-48 rounded-lg border border-line object-cover"
          />
          {uploading ? (
            <div className="absolute inset-0 grid place-items-center rounded-lg bg-ink/50 text-sm font-medium text-white">
              Enviando…
            </div>
          ) : (
            <button
              type="button"
              onClick={clear}
              className="absolute right-2 top-2 rounded-full bg-ink/70 px-2.5 py-1 text-xs font-medium text-white hover:bg-ink"
            >
              Remover
            </button>
          )}
        </div>
      ) : (
        <button
          type="button"
          onClick={() => inputRef.current?.click()}
          disabled={disabled || uploading}
          className="inline-flex items-center gap-2 rounded-lg border border-line bg-canvas px-3 py-2 text-sm font-medium text-ink-soft hover:bg-ink/5 hover:text-ink disabled:opacity-50"
        >
          Escolher do dispositivo
        </button>
      )}
      <input
        ref={inputRef}
        type="file"
        accept={ACCEPT}
        onChange={onPick}
        className="hidden"
      />
      {error ? (
        <p className="mt-2 text-sm font-medium text-slate-700">{error}</p>
      ) : null}
    </div>
  );
}
