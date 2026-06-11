import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { revalidatePath } from 'next/cache';
import { createServerAxios } from '@/lib/axios/serverAxios';

export async function GET() {
  try {
    const api = await createServerAxios();
    const { data } = await api.get('/me/addresses');
    return NextResponse.json(data);
  } catch (error) {
    if (axios.isAxiosError(error)) {
      return NextResponse.json(error.response?.data ?? {}, { status: error.response?.status ?? 500 });
    }
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const api = await createServerAxios();
    const { data } = await api.post('/me/addresses', body);
    revalidatePath('/', 'layout');
    return NextResponse.json(data, { status: 201 });
  } catch (error) {
    if (axios.isAxiosError(error)) {
      return NextResponse.json(error.response?.data ?? {}, { status: error.response?.status ?? 500 });
    }
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}
