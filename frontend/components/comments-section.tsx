"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import {
  ApiError,
  type Comment,
  createPostComment,
  createProductComment,
  deleteComment,
  getPostComments,
  getProductComments,
} from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { formatRelativeTime } from "@/lib/format";
import { Avatar } from "./avatar";
import { IconComment } from "./icons";

export function CommentsSection({
  target,
  targetId,
  ownerUsername,
  preview = false,
}: {
  target: "post" | "product";
  targetId: string;
  ownerUsername: string;
  preview?: boolean;
}) {
  const { user, token } = useAuth();
  const [comments, setComments] = useState<Comment[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState(!preview);
  const [content, setContent] = useState("");
  const [posting, setPosting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const page =
          target === "post"
            ? await getPostComments(targetId)
            : await getProductComments(targetId);
        if (!active) return;
        setComments(page.content);
        setTotal(page.totalElements);
      } catch {
        // mantém vazio em caso de falha
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => {
      active = false;
    };
  }, [target, targetId]);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!token) return;
    const text = content.trim();
    if (!text) return;
    setPosting(true);
    setError(null);
    try {
      const created =
        target === "post"
          ? await createPostComment(token, targetId, text)
          : await createProductComment(token, targetId, text);
      setComments((c) => [created, ...c]);
      setTotal((n) => n + 1);
      setContent("");
      setExpanded(true);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Não foi possível comentar.");
    } finally {
      setPosting(false);
    }
  }

  async function remove(commentId: string) {
    if (!token) return;
    if (!confirm("Apagar este comentário?")) return;
    try {
      await deleteComment(token, commentId);
      setComments((c) => c.filter((x) => x.commentId !== commentId));
      setTotal((n) => Math.max(0, n - 1));
    } catch {
      alert("Não foi possível apagar o comentário.");
    }
  }

  const visible = expanded ? comments : comments.slice(0, 1);

  return (
    <section
      className={`mt-3 border-t border-line pt-3${preview ? " pl-6" : ""}`}
    >
      {loading ? (
        <p className="text-sm text-ink-soft">Carregando comentários…</p>
      ) : (
        <>
          {visible.length > 0 ? (
            <ul className="flex flex-col gap-3">
              {visible.map((c) => {
                const canDelete =
                  !!user &&
                  (user.username === c.author.username ||
                    user.username === ownerUsername);
                return (
                  <li key={c.commentId} className="flex items-start gap-2">
                    <Link href={`/u/${c.author.username}`} className="shrink-0">
                      <Avatar
                        name={c.author.displayName}
                        seed={c.author.username}
                        size={28}
                      />
                    </Link>
                    <div className="min-w-0 flex-1">
                      <p className="text-sm">
                        <Link
                          href={`/u/${c.author.username}`}
                          className="font-semibold text-ink hover:underline"
                        >
                          {c.author.displayName}
                        </Link>{" "}
                        <span className="text-xs text-ink-soft">
                          {formatRelativeTime(c.createdAt)}
                        </span>
                      </p>
                      <p className="whitespace-pre-wrap text-sm text-ink">{c.content}</p>
                    </div>
                    {canDelete ? (
                      <button
                        type="button"
                        onClick={() => remove(c.commentId)}
                        className="shrink-0 text-xs font-medium text-ink-soft hover:text-red-600"
                      >
                        Apagar
                      </button>
                    ) : null}
                  </li>
                );
              })}
            </ul>
          ) : null}

          {preview && total > 1 ? (
            <button
              type="button"
              onClick={() => setExpanded((v) => !v)}
              className="mt-2 text-sm font-medium text-action hover:underline"
            >
              {expanded ? "Ver menos" : `Ver todos os ${total} comentários`}
            </button>
          ) : null}

          {token ? (
            <form onSubmit={submit} className="mt-3 flex items-center gap-2">
              <input
                value={content}
                onChange={(e) => setContent(e.target.value)}
                maxLength={500}
                placeholder="Escreva um comentário…"
                className="min-w-0 flex-1 rounded-full border border-line bg-surface px-4 py-2 text-sm text-ink placeholder:text-ink-soft focus-visible:border-action focus-visible:outline-none"
              />
              <button
                type="submit"
                disabled={posting || !content.trim()}
                aria-label="Comentar"
                title="Comentar"
                className="grid h-9 w-9 shrink-0 place-items-center rounded-full bg-action text-white transition-colors hover:bg-action-strong disabled:opacity-50"
              >
                <IconComment className="h-5 w-5" />
              </button>
            </form>
          ) : (
            <p className="mt-3 text-sm text-ink-soft">
              <Link href="/entrar" className="font-medium text-action hover:underline">
                Entre
              </Link>{" "}
              para comentar.
            </p>
          )}

          {error ? (
            <p className="mt-2 text-sm font-medium text-red-600">{error}</p>
          ) : null}
        </>
      )}
    </section>
  );
}
