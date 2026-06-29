import Link from "next/link";
import { type Post } from "@/lib/api";
import { formatRelativeTime } from "@/lib/format";
import { Avatar } from "./avatar";
import { ProductAttachment } from "./product-attachment";

export function PostCard({ post }: { post: Post }) {
  const isAnuncio = post.type === "ANUNCIO" && post.product != null;
  const image = post.thumbnailUrl ?? post.imageUrl;

  return (
    <article className="rounded-2xl border border-line bg-surface p-5 shadow-sm">
      <header className="flex items-center gap-3">
        <Link href={`/u/${post.owner.username}`} className="shrink-0">
          <Avatar name={post.owner.displayName} seed={post.owner.username} />
        </Link>
        <div className="min-w-0">
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
      </header>

      {post.content ? (
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
