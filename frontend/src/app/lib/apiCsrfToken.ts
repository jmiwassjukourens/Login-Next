const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

let csrfToken: string | null = null;

export async function fetchCsrfToken() {
  const res = await fetch(`${API_BASE_URL}/csrf`, {
    credentials: "include",
  });

  const data = await res.json();
  csrfToken = data.token;
}

export function getCsrfToken() {
  return csrfToken;
}
