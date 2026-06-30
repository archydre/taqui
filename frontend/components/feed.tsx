import { getPosts, type Post } from "@/lib/api";
import { PostCard } from "./post-card";

export async function Feed() {
  let posts: Post[] = [];
  let error = false;

  try {
    const page = await getPosts({ size: 20 });
    posts = page.content;
  } catch {
    error = true;
  }

  if (error) {
    return (
      <p className="rounded-xl border border-slate-300 bg-slate-100 px-4 py-3 text-sm text-slate-700">
        Não foi possível carregar o feed. A API está no ar?
      </p>
    );
  }

  if (posts.length === 0) {
    return (
      <p className="text-ink-soft">Nada no feed por enquanto. Volte mais tarde.</p>
    );
  }

  return (
    <div className="flex flex-col gap-4">
      {posts.map((post) => (
        <PostCard key={post.postId} post={post} />
      ))}
    </div>
  );
}
