
"use client";

import { useAuth } from "../context/AuthContext";
import { redirect } from "next/navigation";

export default function ProtectedLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { user, loading } = useAuth();

  if (loading) {
    return <p>Cargando...</p>;
  }

  if (!user) {
    redirect("/login");
  }

  return <>{children}</>;
}
