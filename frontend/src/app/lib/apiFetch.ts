import { getCookie } from "./getCookie";
import { refreshToken } from "./refreshToken";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";

export async function apiFetch<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {

  const res = await fetch(`${API_BASE_URL}/${endpoint}`, {
    ...options,
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
      ...(getCookie("XSRF-TOKEN") && {
        "X-XSRF-TOKEN": getCookie("XSRF-TOKEN")!,
      }),
    },
  });

  if (res.status === 401) {
    const refreshed = await refreshToken();
    if (refreshed) {
      return apiFetch(endpoint, options);
    }
    throw new Error("Unauthorized");
  }

  return res.json();
}
