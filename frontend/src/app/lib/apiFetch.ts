import { getCookie } from "./getCookie";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export async function apiFetch<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string>),
  };

  const csrfToken = getCookie("XSRF-TOKEN");
  if (csrfToken) {
    headers["X-XSRF-TOKEN"] = csrfToken;
  }

  const res = await fetch(`${API_BASE_URL}/${endpoint}`, {
    ...options,
    credentials: "include", 
    headers,
  });

  if (!res.ok) {
    if (res.status === 401) {
      return null as T;
    }
    throw new Error(`HTTP Error ${res.status}`);
  }

  if (res.status === 204) return null as T;

  return res.json();
}
