import { getCsrfTokenFromCookie } from "./apiCsrfToken";

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

  const csrfToken =  getCsrfTokenFromCookie();

  if (csrfToken && options.method && options.method !== "GET") {
    headers["X-XSRF-TOKEN"] = csrfToken;
  }

  const res = await fetch(`${API_BASE_URL}/${endpoint}`, {
    ...options,
    credentials: "include",
    headers,
  });

  if (!res.ok) {
    throw new Error(`HTTP Error ${res.status}`);
  }

  if (res.status === 204) return null as T;

  return res.json();
}
