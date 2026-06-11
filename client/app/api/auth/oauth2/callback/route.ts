import { NextRequest, NextResponse } from 'next/server';
import { jwtMaxAge, decodeJwt } from '@/lib/jwt';
import { isStaffRole } from '@/lib/constants';

export async function GET(req: NextRequest) {
  const { searchParams } = req.nextUrl;
  const accessToken = searchParams.get('accessToken');
  const refreshToken = searchParams.get('refreshToken');

  if (!accessToken || !refreshToken) {
    return NextResponse.redirect(new URL('/login?error=oauth2_failed', req.url));
  }

  const payload = decodeJwt(accessToken);
  const authorities = (payload?.authorities as string[]) ?? [];
  const isProd = process.env.NODE_ENV === 'production';

  const destination = isStaffRole(authorities) ? '/admin' : '/';
  const res = NextResponse.redirect(new URL(destination, req.url));

  res.cookies.set('access_token', accessToken, {
    httpOnly: true, secure: isProd, sameSite: 'lax',
    maxAge: jwtMaxAge(accessToken, 60 * 10),
    path: '/',
  });
  res.cookies.set('refresh_token', refreshToken, {
    httpOnly: true, secure: isProd, sameSite: 'lax',
    maxAge: jwtMaxAge(refreshToken, 60 * 60 * 24 * 7),
    path: '/',
  });

  return res;
}
