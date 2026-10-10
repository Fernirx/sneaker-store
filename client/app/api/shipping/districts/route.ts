import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { publicAxios } from '@/lib/axios/serverAxios';

export async function GET(req: NextRequest) {
  const provinceId = req.nextUrl.searchParams.get('provinceId');
  if (!provinceId) {
    return NextResponse.json({ message: 'Missing provinceId parameter' }, { status: 400 });
  }
  try {
    const { data } = await publicAxios.get(`/public/shipping/districts?provinceId=${provinceId}`);
    return NextResponse.json(data);
  } catch (error) {
    if (axios.isAxiosError(error)) {
      return NextResponse.json(error.response?.data ?? {}, { status: error.response?.status ?? 500 });
    }
    return NextResponse.json({ message: 'Server error' }, { status: 500 });
  }
}
