"use client";

import { useEffect, useState } from "react";
import { getPosts, type Post } from "@/lib/api";
import { PostCard } from "./post-card";

export function Feed() {
  const [posts, setPosts] = useState<Post[] | null>(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const page = await getPosts({ size: 20 });
        if (active) setPosts(page.content);
      } catch {
        if (active) setError(true);
      }
    })();
    return () => {
      active = false;
    };
  }, []);

  if (error) {
    return (
      <p className="rounded-xl border border-slate-300 bg-slate-100 px-4 py-3 text-sm text-slate-700">
        Não foi possível carregar o feed. A API está no ar?
      </p>
    );
  }

  if (posts === null) {
    return <FeedSkeleton />;
  }

  if (posts.length === 0) {
    return (
      <p className="text-ink-soft">Nada no feed por enquanto. Volte mais tarde.</p>
    );
  }

  return (
    <div className="flex flex-col gap-4">
      {posts.map((post, i) => (
        <div
          key={post.postId}
          className="animate-fade-in-up"
          style={{ animationDelay: `${Math.min(i, 8) * 50}ms` }}
        >
          <PostCard post={post} />
        </div>
      ))}
    </div>
  );
}

function FeedSkeleton() {
  return (
    <div className="flex flex-col gap-4" aria-hidden="true">
      {Array.from({ length: 4 }).map((_, i) => (
        <div
          key={i}
          className="rounded-2xl border border-line bg-surface p-5 shadow-sm"
        >
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 animate-pulse rounded-full bg-line" />
            <div className="flex flex-col gap-2">
              <div className="h-3 w-32 animate-pulse rounded bg-line" />
              <div className="h-3 w-20 animate-pulse rounded bg-line" />
            </div>
          </div>
          <div className="mt-4 h-3 w-full animate-pulse rounded bg-line" />
          <div className="mt-2 h-3 w-2/3 animate-pulse rounded bg-line" />
          <div className="mt-4 aspect-video w-full animate-pulse rounded-xl bg-line" />
        </div>
      ))}
    </div>
  );
}
