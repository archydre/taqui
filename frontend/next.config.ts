import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // standalone é para a imagem Docker; na Vercel (que define VERCEL=1) usamos o build padrão.
  output: process.env.VERCEL ? undefined : "standalone",
};

export default nextConfig;
