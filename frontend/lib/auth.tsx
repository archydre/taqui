"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import { getMe, setUnauthorizedHandler, type Me } from "@/lib/api";

const TOKEN_KEY = "taqui_token";

type AuthState = {
  token: string | null;
  user: Me | null;
  loading: boolean;
  login: (token: string) => Promise<void>;
  logout: () => void;
  refresh: () => Promise<void>;
};

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<Me | null>(null);
  const [loading, setLoading] = useState(true);

  const loadUser = useCallback(async (activeToken: string) => {
    try {
      const me = await getMe(activeToken);
      setUser(me);
    } catch {
      window.localStorage.removeItem(TOKEN_KEY);
      setToken(null);
      setUser(null);
    }
  }, []);

  useEffect(() => {
    const init = async () => {
      const stored = window.localStorage.getItem(TOKEN_KEY);
      if (stored) {
        setToken(stored);
        await loadUser(stored);
      }
      setLoading(false);
    };
    void init();
  }, [loadUser]);

  const login = useCallback(
    async (newToken: string) => {
      window.localStorage.setItem(TOKEN_KEY, newToken);
      setToken(newToken);
      await loadUser(newToken);
    },
    [loadUser],
  );

  const logout = useCallback(() => {
    window.localStorage.removeItem(TOKEN_KEY);
    setToken(null);
    setUser(null);
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(logout);
    return () => setUnauthorizedHandler(null);
  }, [logout]);

  const refresh = useCallback(async () => {
    if (token) await loadUser(token);
  }, [token, loadUser]);

  return (
    <AuthContext value={{ token, user, loading, login, logout, refresh }}>
      {children}
    </AuthContext>
  );
}

export function useAuth(): AuthState {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth precisa estar dentro de <AuthProvider>");
  }
  return context;
}
