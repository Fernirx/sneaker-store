import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { createServerAxios } from '@/lib/axios/serverAxios';

export async function GET() {
  try {
    const api = await createServerAxios();
    const { data } = await api.get('/me');
    return NextResponse.json(data);
  } catch (error) {
    if (axios.isAxiosError(error)) {
      return NextResponse.json(error.response?.data ?? {}, { status: error.response?.status ?? 500 });
    }
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}

export async function PATCH(req: NextRequest) {
  try {
    const body = await req.json();
    const api = await createServerAxios();
    const { data } = await api.patch('/me', body);
    return NextResponse.json(data);
  } catch (error) {
    if (axios.isAxiosError(error)) {
      return NextResponse.json(error.response?.data ?? {}, { status: error.response?.status ?? 500 });
    }
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}
