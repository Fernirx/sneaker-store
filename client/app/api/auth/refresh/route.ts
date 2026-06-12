import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { cookies } from 'next/headers';
import { jwtMaxAge } from '@/lib/jwt';

export async function POST(_req: NextRequest) {
  const store = await cookies();
  const refreshToken = store.get('refresh_token')?.value;
  const isProd = process.env.NODE_ENV === 'production';

  if (!refreshToken) {
    return NextResponse.json({ message: 'No refresh token' }, { status: 401 });
  }

  try {
    // Gọi thẳng không dùng createServerAxios — refresh endpoint là public,
    // gửi expired access token sẽ bị JwtAuthenticationFilter reject trước khi xử lý
    const { data } = await axios.post(
      `${process.env.SPRING_API_URL}/auth/refresh-token`,
      { refreshToken },
      { headers: { 'Content-Type': 'application/json' } },
    );

    const { accessToken, refreshToken: newRefresh } = data.data;

    const res = NextResponse.json({ ok: true });
    res.cookies.set('access_token', accessToken, {
      httpOnly: true, secure: isProd, sameSite: 'lax',
      maxAge: jwtMaxAge(accessToken, 60 * 10),
      path: '/',
    });
    res.cookies.set('refresh_token', newRefresh, {
      httpOnly: true, secure: isProd, sameSite: 'lax',
      maxAge: jwtMaxAge(newRefresh, 60 * 60 * 24 * 7),
      path: '/',
    });

    return res;
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      const res = NextResponse.json({ message: 'Refresh failed' }, { status: 401 });
      res.cookies.delete('access_token');
      res.cookies.delete('refresh_token');
      return res;
    }
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}
