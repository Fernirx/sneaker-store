import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { createServerAxios } from '@/lib/axios/serverAxios';

export async function GET(request: NextRequest) {
  try {
    const topLimit = request.nextUrl.searchParams.get('topLimit') || '5';
    const revenueDays = request.nextUrl.searchParams.get('revenueDays') || '14';
    const api = await createServerAxios();
    const { data } = await api.get(`/internal/dashboard/summary?topLimit=${topLimit}&revenueDays=${revenueDays}`);
    return NextResponse.json(data);
  } catch (error) {
    if (axios.isAxiosError(error)) {
      return NextResponse.json(error.response?.data ?? {}, { status: error.response?.status ?? 500 });
    }
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}
