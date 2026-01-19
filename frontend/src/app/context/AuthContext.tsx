"use client";

import { createContext, useContext, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { apiFetch } from "../lib/apiFetch";

type AuthContextType = {
  user: string | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  setUser: (user: string | null) => void;
};

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();


  useEffect(() => {
    checkAuth();
  }, []);


  const login = async (username: string, password: string) => {
    setLoading(true);
    try {
      await apiFetch("login", {
        method: "POST",
        body: JSON.stringify({ username, password }),
      });


      await checkAuth();

      router.replace("/dashboard");
    } finally {
      setLoading(false);
    }
  };


  const logout = async () => {
    setLoading(true);
    try {
      await apiFetch("users/logout", { method: "POST" });
      setUser(null);
      router.push("/login");
    } finally {
      setLoading(false);
    }
  };

  const checkAuth = async () => {
    try {
      const res = await apiFetch<{ username: string }>("users/me");
      setUser(res.username);
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthContext.Provider
      value={{ user, loading, login, logout, setUser }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return ctx;
};
