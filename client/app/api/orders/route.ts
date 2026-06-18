import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { createServerAxios } from '@/lib/axios/serverAxios';

function guestHeader(req: NextRequest): Record<string, string> {
  const t = req.headers.get('X-Guest-Token');
  return t ? { 'X-Guest-Token': t } : {};
}

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const api = await createServerAxios();
    const { data } = await api.post('/orders', body, { headers: guestHeader(req) });
    return NextResponse.json(data);
  } catch (err) {
    if (axios.isAxiosError(err))
      return NextResponse.json(err.response?.data ?? {}, { status: err.response?.status ?? 500 });
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}

export async function GET(req: NextRequest) {
  const search = req.nextUrl.searchParams.toString();
  try {
    const api = await createServerAxios();
    const { data } = await api.get(`/orders${search ? `?${search}` : ''}`, { headers: guestHeader(req) });
    return NextResponse.json(data);
  } catch (err) {
    if (axios.isAxiosError(err))
      return NextResponse.json(err.response?.data ?? {}, { status: err.response?.status ?? 500 });
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}
