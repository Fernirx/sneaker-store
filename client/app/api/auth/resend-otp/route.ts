import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { createServerAxios } from '@/lib/axios/serverAxios';

export async function POST(req: NextRequest) {
  const body = await req.json();
  try {
    const api = await createServerAxios();
    await api.post('/auth/resend-otp', body);
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
