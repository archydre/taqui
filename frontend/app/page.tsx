import { Composer } from "@/components/composer";
import { Feed } from "@/components/feed";
import { FeedDivider } from "@/components/feed-divider";

export default function Home() {
  return (
    <div className="mx-auto w-full max-w-xl">
      <h1 className="sr-only">Feed</h1>
      <Composer />
      <FeedDivider />
      <Feed />
    </div>
  );
}
