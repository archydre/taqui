"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { ApiError, createPost } from "@/lib/api";
import { Avatar } from "./avatar";
import { ImagePicker } from "./image-picker";
import { IconImagePlus } from "./icons";

export function Composer() {
  const { user, token, loading } = useAuth();
  const router = useRouter();
  const [content, setContent] = useState("");
  const [imageUrl, setImageUrl] = useState<string | null>(null);
  const [thumbnailUrl, setThumbnailUrl] = useState<string | null>(null);
  const [showImage, setShowImage] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [imageUploading, setImageUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (loading) {
    return (
      <div
        className="mb-4 rounded-2xl border border-line bg-surface p-5 shadow-sm"
        aria-hidden="true"
      >
        <div className="flex gap-3">
          <div className="h-10 w-10 animate-pulse rounded-full bg-line" />
          <div className="flex-1">
            <div className="h-4 w-2/3 animate-pulse rounded bg-line" />
            <div className="mt-3 ml-auto h-9 w-28 animate-pulse rounded-full bg-line" />
          </div>
        </div>
      </div>
    );
  }

  if (!user || !token) {
    return (
      <div className="mb-4 rounded-2xl border border-line bg-surface p-5 text-center shadow-sm">
        <p className="text-ink-soft">
          <Link href="/entrar" className="font-medium text-action hover:underline">
            Entre
          </Link>{" "}
          para publicar no feed e vender.
        </p>
      </div>
    );
  }

  async function publish() {
    if (!token) return;
    if (imageUploading) {
      setError("Aguarde o envio da imagem terminar.");
      return;
    }
    const trimmed = content.trim();
    if (!trimmed && !imageUrl) {
      setError("Escreva algo ou adicione uma imagem.");
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await createPost(token, {
        content: trimmed || undefined,
        imageUrl: imageUrl || undefined,
        thumbnailUrl: thumbnailUrl || undefined,
      });
      setContent("");
      setImageUrl(null);
      setThumbnailUrl(null);
      setShowImage(false);
      router.refresh();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Não deu para publicar.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="mb-4 rounded-2xl border border-line bg-surface p-5 shadow-sm">
      <div className="flex gap-3">
        <Avatar name={user.displayName} seed={user.username} />
        <div className="flex-1">
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="O que você está vendendo ou procurando?"
            rows={2}
            maxLength={1000}
            className="w-full resize-none bg-transparent text-[15px] text-ink placeholder:text-ink-soft focus:outline-none"
          />

          {showImage ? (
            <div className="mt-2">
              <ImagePicker
                token={token}
                imageUrl={imageUrl}
                onUploaded={(urls) => {
                  setImageUrl(urls.imageUrl);
                  setThumbnailUrl(urls.thumbnailUrl);
                }}
                onClear={() => {
                  setImageUrl(null);
                  setThumbnailUrl(null);
                }}
                onUploadingChange={setImageUploading}
              />
            </div>
          ) : null}

          {error ? <p className="mt-2 text-sm font-medium text-slate-700">{error}</p> : null}

          <div className="mt-3 flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={() => {
                if (showImage) {
                  setImageUrl(null);
                  setThumbnailUrl(null);
                }
                setShowImage((v) => !v);
              }}
              aria-label={showImage ? "Remover imagem" : "Adicionar imagem"}
              aria-pressed={showImage}
              title={showImage ? "Remover imagem" : "Adicionar imagem"}
              className={`grid h-9 w-9 place-items-center rounded-full transition-colors hover:bg-ink/5 ${
                showImage ? "text-action" : "text-ink-soft hover:text-ink"
              }`}
            >
              <IconImagePlus className="h-5 w-5" />
            </button>
            <button
              type="button"
              onClick={publish}
              disabled={submitting || imageUploading}
              className="rounded-full bg-action px-5 py-2 text-sm font-semibold text-white transition-colors hover:bg-action-strong disabled:opacity-50"
            >
              {imageUploading
                ? "Enviando imagem…"
                : submitting
                  ? "Publicando…"
                  : "Publicar"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
