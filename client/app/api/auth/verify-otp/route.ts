import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { publicAxios } from '@/lib/axios/serverAxios';
import { decodeJwt, jwtMaxAge } from '@/lib/jwt';

export async function POST(req: NextRequest) {
  const body = await req.json();
  const isProd = process.env.NODE_ENV === 'production';
  const locale = req.cookies.get('NEXT_LOCALE')?.value ?? 'vi';

  try {
    const { data } = await publicAxios.post('/auth/verify-otp', body, { headers: { 'Accept-Language': locale } });

    const { accessToken, refreshToken } = data.data;
    const payload = decodeJwt(accessToken);

    const res = NextResponse.json({
      roles: (payload?.authorities as string[]) ?? [],
    });

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
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      return NextResponse.json(
        error.response?.data ?? { message: 'Xác minh thất bại' },
        { status: error.response?.status ?? 500 },
      );
    }
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}
