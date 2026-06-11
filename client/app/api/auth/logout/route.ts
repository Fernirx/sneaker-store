import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';
import { createServerAxios } from '@/lib/axios/serverAxios';

export async function POST(_req: NextRequest) {
  const store = await cookies();
  const accessToken = store.get('access_token')?.value;
  const refreshToken = store.get('refresh_token')?.value;

  if (accessToken && refreshToken) {
    try {
      const api = await createServerAxios();
      await api.post('/auth/logout', { accessToken, refreshToken });
    } catch {
      // Xóa cookie dù BE lỗi
    }
  }

  const res = NextResponse.json({ ok: true });
  res.cookies.delete('access_token');
  res.cookies.delete('refresh_token');
  return res;
}
