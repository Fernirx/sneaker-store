import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { createServerAxios } from '@/lib/axios/serverAxios';

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const guestToken = req.headers.get('X-Guest-Token');
    const api = await createServerAxios();
    const { data } = await api.post('/cart/buy-now', body, {
      headers: guestToken ? { 'X-Guest-Token': guestToken } : {},
    });
    return NextResponse.json(data);
  } catch (err) {
    if (axios.isAxiosError(err))
      return NextResponse.json(err.response?.data ?? {}, { status: err.response?.status ?? 500 });
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}
