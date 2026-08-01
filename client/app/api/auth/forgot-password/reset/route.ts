import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { publicAxios } from '@/lib/axios/serverAxios';

export async function POST(req: NextRequest) {
  const body = await req.json();
  try {
    const { data } = await publicAxios.post('/auth/forgot-password/reset', body);
    return NextResponse.json(data);
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      return NextResponse.json(
        error.response?.data ?? { message: 'Đặt lại mật khẩu thất bại' },
        { status: error.response?.status ?? 500 },
      );
    }
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}
