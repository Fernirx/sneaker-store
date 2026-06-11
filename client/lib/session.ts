import { cookies } from 'next/headers';
import { decodeJwt } from './jwt';

interface Session {
  userId: string;
  roles: string[];
  email?: string;
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
