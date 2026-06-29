"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { ApiError, createPost } from "@/lib/api";
import { Avatar } from "./avatar";

export function Composer() {
  const { user, token, loading } = useAuth();
  const router = useRouter();
  const [content, setContent] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [showImage, setShowImage] = useState(false);
  const [submitting, setSubmitting] = useState(false);
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
    const trimmed = content.trim();
    const image = imageUrl.trim();
    if (!trimmed && !image) {
      setError("Escreva algo ou adicione uma imagem.");
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await createPost(token, {
        content: trimmed || undefined,
        imageUrl: image || undefined,
      });
      setContent("");
      setImageUrl("");
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
            <input
              type="url"
              value={imageUrl}
              onChange={(e) => setImageUrl(e.target.value)}
              placeholder="Cole a URL de uma imagem…"
              className="mt-2 w-full rounded-lg border border-line bg-canvas px-3 py-2 text-sm text-ink placeholder:text-ink-soft focus-visible:border-action focus-visible:outline-none"
            />
          ) : null}

          {error ? <p className="mt-2 text-sm text-red-600">{error}</p> : null}

          <div className="mt-3 flex items-center justify-between">
            <button
              type="button"
              onClick={() => setShowImage((v) => !v)}
              className="rounded-full px-3 py-1.5 text-sm font-medium text-ink-soft hover:bg-ink/5 hover:text-ink"
            >
              {showImage ? "Remover imagem" : "Adicionar imagem"}
            </button>
            <button
              type="button"
              onClick={publish}
              disabled={submitting}
              className="rounded-full bg-action px-5 py-2 text-sm font-semibold text-white transition-colors hover:bg-action-strong disabled:opacity-50"
            >
              {submitting ? "Publicando…" : "Publicar"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
