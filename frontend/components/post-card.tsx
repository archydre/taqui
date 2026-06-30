"use client";

import { useState, useRef, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { type Post, deletePost, updatePost } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { formatRelativeTime } from "@/lib/format";
import { Avatar } from "./avatar";
import { ProductAttachment } from "./product-attachment";

export function PostCard({ post }: { post: Post }) {
  const { user, token } = useAuth();
  const router = useRouter();
  const isOwner = !!user && user.username === post.owner.username;
  const isAnuncio = post.type === "ANUNCIO" && post.product != null;
  const image = post.thumbnailUrl ?? post.imageUrl;

  const [menuOpen, setMenuOpen] = useState(false);
  const [editing, setEditing] = useState(false);
  const [editContent, setEditContent] = useState(post.content ?? "");
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!menuOpen) return;
    function close(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    }
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, [menuOpen]);

  async function handleDelete() {
    if (!token) return;
    if (!confirm("Excluir esta publicação?")) return;
    setDeleting(true);
    try {
      await deletePost(token, post.postId);
      router.refresh();
    } catch {
      alert("Não foi possível excluir.");
      setDeleting(false);
    }
  }

  async function handleSaveEdit() {
    if (!token) return;
    setSaving(true);
    try {
      await updatePost(token, post.postId, {
        content: editContent || undefined,
        imageUrl: post.imageUrl ?? undefined,
        thumbnailUrl: post.thumbnailUrl ?? undefined,
        productId: post.product?.productId ?? undefined,
      });
      router.refresh();
      setEditing(false);
    } catch {
      alert("Não foi possível salvar.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <article className="rounded-2xl border border-line bg-surface p-5 shadow-sm">
      <header className="flex items-center gap-3">
        <Link href={`/u/${post.owner.username}`} className="shrink-0">
          <Avatar name={post.owner.displayName} seed={post.owner.username} />
        </Link>
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-2">
            <Link
              href={`/u/${post.owner.username}`}
              className="truncate font-semibold text-ink hover:underline"
            >
              {post.owner.displayName}
            </Link>
            {isAnuncio ? (
              <span className="rounded-full bg-action/10 px-2 py-0.5 text-xs font-medium text-action">
                Anúncio
              </span>
            ) : null}
          </div>
          <p className="truncate text-sm text-ink-soft">
            @{post.owner.username} · {formatRelativeTime(post.createdAt)}
          </p>
        </div>

        {isOwner ? (
          <div className="relative shrink-0" ref={menuRef}>
            <button
              type="button"
              onClick={() => setMenuOpen((o) => !o)}
              disabled={deleting}
              className="flex h-8 w-8 items-center justify-center rounded-full text-ink-soft hover:bg-ink/5 disabled:opacity-40"
              aria-label="Opções"
            >
              ···
            </button>
            {menuOpen ? (
              <div className="absolute right-0 top-9 z-10 min-w-[140px] overflow-hidden rounded-xl border border-line bg-surface shadow-lg">
                <button
                  type="button"
                  className="w-full px-4 py-2.5 text-left text-sm text-ink hover:bg-ink/5"
                  onClick={() => {
                    setEditing(true);
                    setMenuOpen(false);
                  }}
                >
                  Editar
                </button>
                <button
                  type="button"
                  className="w-full px-4 py-2.5 text-left text-sm text-red-600 hover:bg-red-50"
                  onClick={() => {
                    setMenuOpen(false);
                    handleDelete();
                  }}
                >
                  Excluir
                </button>
              </div>
            ) : null}
          </div>
        ) : null}
      </header>

      {editing ? (
        <div className="mt-3 flex flex-col gap-2">
          <textarea
            rows={3}
            value={editContent}
            onChange={(e) => setEditContent(e.target.value)}
            maxLength={1000}
            className="w-full resize-none rounded-lg border border-line bg-canvas px-3 py-2 text-[15px] text-ink focus-visible:border-action focus-visible:outline-none"
          />
          <div className="flex gap-2">
            <button
              type="button"
              onClick={handleSaveEdit}
              disabled={saving}
              className="rounded-full bg-action px-4 py-1.5 text-sm font-semibold text-white hover:bg-action-strong disabled:opacity-50"
            >
              {saving ? "Salvando…" : "Salvar"}
            </button>
            <button
              type="button"
              onClick={() => {
                setEditing(false);
                setEditContent(post.content ?? "");
              }}
              className="rounded-full border border-line px-4 py-1.5 text-sm text-ink hover:bg-ink/5"
            >
              Cancelar
            </button>
          </div>
        </div>
      ) : post.content ? (
        <p className="mt-3 text-[15px] leading-relaxed whitespace-pre-wrap text-ink">
          {post.content}
        </p>
      ) : null}

      {image ? (
        <div className="mt-3 overflow-hidden rounded-xl border border-line">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src={image}
            alt=""
            loading="lazy"
            className="max-h-[28rem] w-full object-cover"
          />
        </div>
      ) : null}

      {isAnuncio && post.product ? (
        <ProductAttachment product={post.product} />
      ) : null}
    </article>
  );
}
