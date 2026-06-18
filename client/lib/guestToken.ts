const GUEST_TOKEN_KEY = 'guest_cart_token';

export function getGuestToken(): string | null {
  try { return localStorage.getItem(GUEST_TOKEN_KEY); } catch { return null; }
}

export function saveGuestToken(token: string | null) {
  try { if (token) localStorage.setItem(GUEST_TOKEN_KEY, token); } catch { /* ignore */ }
}

export function clearGuestToken() {
  try { localStorage.removeItem(GUEST_TOKEN_KEY); } catch { /* ignore */ }
}

export function guestHeaders(): Record<string, string> {
  const t = getGuestToken();
  return t ? { 'X-Guest-Token': t } : {};
}
