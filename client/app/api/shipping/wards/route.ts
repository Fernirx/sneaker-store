import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { publicAxios } from '@/lib/axios/serverAxios';

export async function GET(req: NextRequest) {
  const districtId = req.nextUrl.searchParams.get('districtId');
  if (!districtId) {
    return NextResponse.json({ message: 'Missing districtId parameter' }, { status: 400 });
  }
  try {
    const { data } = await publicAxios.get(`/public/shipping/wards?districtId=${districtId}`);
    return NextResponse.json(data);
  } catch (error) {
    if (axios.isAxiosError(error)) {
      return NextResponse.json(error.response?.data ?? {}, { status: error.response?.status ?? 500 });
    }
    return NextResponse.json({ message: 'Server error' }, { status: 500 });
  }
}
