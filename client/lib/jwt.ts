export function decodeJwt(token: string): Record<string, unknown> | null {
  try {
    return JSON.parse(Buffer.from(token.split('.')[1], 'base64url').toString());
  } catch {
    return null;
  }
}

export function jwtMaxAge(token: string, fallbackSeconds: number): number {
  const payload = decodeJwt(token);
  if (!payload?.exp) return fallbackSeconds;
  const remaining = (payload.exp as number) - Math.floor(Date.now() / 1000);
  return remaining > 0 ? remaining : fallbackSeconds;
}
