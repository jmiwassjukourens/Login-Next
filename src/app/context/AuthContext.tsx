"use client";

import { createContext, useContext, useState } from "react";
import { apiFetch } from "../lib/apiFetch";
import { useRouter } from "next/navigation";

type AuthContextType = {
  user: string | null;
  login: (u: string, p: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<string | null>(() => {
  if (typeof window === "undefined") return null;
  return sessionStorage.getItem("user");
});

  const router = useRouter();


  const login = async (username: string, password: string) => {
    const data = await apiFetch<{ token: string; username: string }>(
      "login",
      {
        method: "POST",
        body: JSON.stringify({ username, password }),
      }
    );

    sessionStorage.setItem("token", data.token);
    sessionStorage.setItem("user", data.username);
    setUser(data.username);

    router.push("/dashboard");
  };

  const logout = () => {
    sessionStorage.clear();
    setUser(null);
    router.push("/login");
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext)!;
