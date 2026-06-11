import { cookies } from 'next/headers';

interface Session {
  userId: string;
  roles: string[];
  email?: string;
}

function decodeJwt(token: string): Record<string, unknown> | null {
  try {
    const payload = token.split('.')[1];
    return JSON.parse(Buffer.from(payload, 'base64url').toString());
  } catch {
    return null;
  }
}

export async function getSession(): Promise<Session | null> {
  const store = await cookies();
  const token = store.get('access_token')?.value;
  if (!token) return null;

  const payload = decodeJwt(token);
  if (!payload) return null;

  const exp = payload.exp as number;
  if (Date.now() / 1000 > exp) return null;

  return {
    userId: payload.sub as string,
    roles: (payload.authorities as string[]) ?? [],
    email: payload.email as string | undefined,
  };
}

export { isStaffRole as isAdminRole } from './constants';
