import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { createServerAxios } from '@/lib/axios/serverAxios';
import { decodeJwt, jwtMaxAge } from '@/lib/jwt';

export async function POST(req: NextRequest) {
  const body = await req.json();
  const isProd = process.env.NODE_ENV === 'production';

  try {
    const api = await createServerAxios();
    const { data } = await api.post('/auth/login', body);

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
        error.response?.data ?? { message: 'Lỗi đăng nhập' },
        { status: error.response?.status ?? 500 },
      );
    }
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}
