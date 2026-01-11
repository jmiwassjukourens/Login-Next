const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export async function apiFetch<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token =
    typeof window !== "undefined"
      ? sessionStorage.getItem("token")
      : null;

  const res = await fetch(`${API_BASE_URL}/${endpoint}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    },
  });

  if (!res.ok) {
    if (res.status === 401 && typeof window !== "undefined") {
      sessionStorage.removeItem("token");
      window.location.href = "/login";
    }

    throw new Error(`HTTP Error ${res.status}`);
  }

  if (res.status === 204) return null as T;

  return res.json();
}
