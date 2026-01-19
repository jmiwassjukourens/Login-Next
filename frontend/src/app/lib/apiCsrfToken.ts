export function getCsrfTokenFromCookie(): string | null {
  if (typeof document === "undefined") return null;

  return document.cookie
    .split("; ")
    .find(row => row.startsWith("XSRF-TOKEN="))
    ?.split("=")[1] ?? null;
}
