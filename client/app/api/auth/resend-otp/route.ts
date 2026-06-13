import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { publicAxios } from '@/lib/axios/serverAxios';

export async function POST(req: NextRequest) {
  const body = await req.json();
  const locale = req.cookies.get('NEXT_LOCALE')?.value ?? 'vi';
  try {
    await publicAxios.post('/auth/resend-otp', body, { headers: { 'Accept-Language': locale } });
    return NextResponse.json({ ok: true });
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      return NextResponse.json(
        error.response?.data ?? { message: 'Gửi lại OTP thất bại' },
        { status: error.response?.status ?? 500 },
      );
    }
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}
