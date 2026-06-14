import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { publicAxios } from '@/lib/axios/serverAxios';

export async function GET(req: NextRequest) {
  const search = req.nextUrl.searchParams.toString();
  try {
    const { data } = await publicAxios.get(`/products${search ? `?${search}` : ''}`);
    return NextResponse.json(data);
  } catch (error) {
    if (axios.isAxiosError(error)) {
      return NextResponse.json(error.response?.data ?? {}, { status: error.response?.status ?? 500 });
    }
    return NextResponse.json({ message: 'Server error' }, { status: 500 });
  }
}
