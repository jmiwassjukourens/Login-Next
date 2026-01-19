"use client";

import { useState } from "react";
import { apiFetch } from "../lib/apiFetch";
import { useRouter } from "next/navigation";

export default function CreateUserPage() {
  const router = useRouter();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      await apiFetch("users", {
        method: "POST",
        body: JSON.stringify({
          username,
          password,
        }),
      });

      router.push("/dashboard"); 
    } catch (err) {
      setError("Error al crear el usuario" + (err instanceof Error ? err.message : ""));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 400, margin: "40px auto" }}>
      <h1>Crear usuario</h1>

      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: 12 }}>
          <label>Usuario</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            minLength={4}
          />
        </div>

        <div style={{ marginBottom: 12 }}>
          <label>Contraseña</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>

        {error && (
          <p style={{ color: "red", marginBottom: 12 }}>{error}</p>
        )}

        <button type="submit" disabled={loading}>
          {loading ? "Creando..." : "Crear usuario"}
        </button>
      </form>
    </div>
  );
}
