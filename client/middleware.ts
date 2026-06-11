import createIntlMiddleware from 'next-intl/middleware';
import { type NextRequest, NextResponse } from 'next/server';
import { routing } from './i18n/routing';
import { isStaffRole } from './lib/constants';

const intl = createIntlMiddleware(routing);

function decodeJwt(token: string) {
  try {
    return JSON.parse(Buffer.from(token.split('.')[1], 'base64url').toString());
  } catch {
    return null;
  }
}

const ADMIN_RE = /^(?:\/en)?\/admin(?:\/|$)/;
const AUTH_RE  = /^(?:\/en)?\/(?:login|register)(?:\/|$)/;

export default function middleware(req: NextRequest) {
  const { pathname } = req.nextUrl;

  if (pathname.startsWith('/api')) return NextResponse.next();

  const token = req.cookies.get('access_token')?.value;
  const payload = token ? decodeJwt(token) : null;
  const isLoggedIn = !!payload && Date.now() / 1000 < (payload.exp ?? 0);
  const roles: string[] = payload?.authorities ?? [];
  const isAdmin = isStaffRole(roles);

  // Đang ở trang auth nhưng đã login → redirect về đúng nơi
  if (AUTH_RE.test(pathname) && isLoggedIn) {
    const base = req.nextUrl.pathname.startsWith('/en') ? '/en' : '';
    return NextResponse.redirect(new URL(isAdmin ? `${base}/admin` : `${base}/`, req.url));
  }

  // Vào admin chưa login
  if (ADMIN_RE.test(pathname) && !isLoggedIn) {
    const base = req.nextUrl.pathname.startsWith('/en') ? '/en' : '';
    return NextResponse.redirect(new URL(`${base}/login`, req.url));
  }

  // Vào admin nhưng không có role
  if (ADMIN_RE.test(pathname) && isLoggedIn && !isAdmin) {
    return NextResponse.redirect(new URL('/', req.url));
  }

  return intl(req);
}

export const config = {
  matcher: ['/((?!_next|.*\\..*).*)'],
};
