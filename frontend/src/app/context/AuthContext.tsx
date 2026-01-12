"use client";

import { createContext, useContext, useEffect, useState } from "react";
import { apiFetch } from "../lib/apiFetch";
import { fetchCsrfToken } from "../lib/apiCsrfToken";
import { useRouter } from "next/navigation";

type LoginResponse = {
  message: string;
  username: string;
};

type MeResponse = {
  username: string;
};

type AuthContextType = {
  user: string | null;
  loading: boolean;
  login: (u: string, p: string) => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();


  useEffect(() => {
    fetchCsrfToken();
  }, []);


  useEffect(() => {
    apiFetch<MeResponse>("users/me")
      .then(res => setUser(res.username))
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  const login = async (username: string, password: string) => {
    const res = await apiFetch<LoginResponse>("login", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    });

    setUser(res.username);
    router.push("/dashboard");
  };

  const logout = async () => {
    await apiFetch("logout", { method: "POST" });
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext)!;