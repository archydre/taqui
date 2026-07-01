import type { Metadata } from "next";
import { Sora, Inter } from "next/font/google";
import "./globals.css";
import { AppShell } from "@/components/app-shell";
import { AuthProvider } from "@/lib/auth";

const sora = Sora({
  variable: "--font-sora",
  subsets: ["latin"],
  weight: ["500", "600", "700"],
});

const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "taqui",
  description: "A vitrine de quem vende pertinho de você.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="pt-BR"
      suppressHydrationWarning
      className={`${sora.variable} ${inter.variable} h-full antialiased`}
    >
      <body className="min-h-full">
        {/* Decide o welcome ANTES da pintura (evita flash do feed atrás). */}
        <script
          dangerouslySetInnerHTML={{
            __html: `(function(){try{var p=location.pathname;if(p==="/entrar"||p==="/cadastrar"||p==="/verify")return;if(localStorage.getItem("taqui_token"))return;var A="taqui_welcome_active",C="taqui_welcome_count",raw=localStorage.getItem(C),show,na,nc;if(localStorage.getItem(A)==="1"){show=true;na="1";nc=parseInt(raw||"0",10)||0}else{var n=(parseInt(raw||"0",10)||0)+1;if(raw===null||n>=5){show=true;na="1";nc=0}else{show=false;na="0";nc=n}}localStorage.setItem(A,na);localStorage.setItem(C,""+nc);if(show)document.documentElement.classList.add("welcome-open")}catch(e){}})();`,
          }}
        />
        <AuthProvider>
          <AppShell>{children}</AppShell>
        </AuthProvider>
      </body>
    </html>
  );
}
