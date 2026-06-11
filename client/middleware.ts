import createIntlMiddleware from 'next-intl/middleware';
import { type NextRequest, NextResponse } from 'next/server';
import { routing } from './i18n/routing';
import { isStaffRole } from './lib/constants';

const intl = createIntlMiddleware(routing);
const isProd = process.env.NODE_ENV === 'production';

function decodeJwt(token: string) {
  try {
    return JSON.parse(Buffer.from(token.split('.')[1], 'base64url').toString());
  } catch {
    return null;
  }
}

function jwtMaxAge(token: string, fallback: number): number {
  const payload = decodeJwt(token);
  if (!payload?.exp) return fallback;
  const remaining = (payload.exp as number) - Math.floor(Date.now() / 1000);
  return remaining > 0 ? remaining : fallback;
}

function setTokenCookies(res: NextResponse, tokens: { accessToken: string; refreshToken: string }) {
  res.cookies.set('access_token', tokens.accessToken, {
    httpOnly: true, secure: isProd, sameSite: 'lax',
    maxAge: jwtMaxAge(tokens.accessToken, 60 * 10),
    path: '/',
  });
  res.cookies.set('refresh_token', tokens.refreshToken, {
    httpOnly: true, secure: isProd, sameSite: 'lax',
    maxAge: jwtMaxAge(tokens.refreshToken, 60 * 60 * 24 * 7),
    path: '/',
  });
}

async function tryRefresh(refreshToken: string) {
  try {
    const res = await fetch(`${process.env.SPRING_API_URL}/auth/refresh-token`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    });
    if (!res.ok) return null;
    const json = await res.json();
    return json.data as { accessToken: string; refreshToken: string };
  } catch {
    return null;
  }
}

const ADMIN_RE = /^(?:\/en)?\/admin(?:\/|$)/;
const AUTH_RE  = /^(?:\/en)?\/(?:login|register)(?:\/|$)/;

export default async function middleware(req: NextRequest) {
  const { pathname } = req.nextUrl;

  if (pathname.startsWith('/api')) return NextResponse.next();

  const accessCookie = req.cookies.get('access_token')?.value;
  const refreshCookie = req.cookies.get('refresh_token')?.value;

  let payload = accessCookie ? decodeJwt(accessCookie) : null;
  let isLoggedIn = !!payload && Date.now() / 1000 < (payload.exp ?? 0);

  // Access token hết hạn → refresh → redirect cùng URL để browser gửi lại request với cookie mới
  if (!isLoggedIn && refreshCookie) {
    const newTokens = await tryRefresh(refreshCookie);
    if (newTokens) {
      const res = NextResponse.redirect(req.url);
      setTokenCookies(res, newTokens);
      return res;
    }
  }

  const roles: string[] = payload?.authorities ?? [];
  const isAdmin = isStaffRole(roles);

  if (AUTH_RE.test(pathname) && isLoggedIn) {
    const base = pathname.startsWith('/en') ? '/en' : '';
    return NextResponse.redirect(new URL(isAdmin ? `${base}/admin` : `${base}/`, req.url));
  }

  if (ADMIN_RE.test(pathname) && !isLoggedIn) {
    const base = pathname.startsWith('/en') ? '/en' : '';
    return NextResponse.redirect(new URL(`${base}/login`, req.url));
  }

  if (ADMIN_RE.test(pathname) && isLoggedIn && !isAdmin) {
    return NextResponse.redirect(new URL('/', req.url));
  }

  return intl(req) as NextResponse;
}

export const config = {
  matcher: ['/((?!_next|.*\\..*).*)'],
};
