import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { createServerAxios } from '@/lib/axios/serverAxios';

export async function GET(
  req: NextRequest,
  props: { params: Promise<{ token: string }> },
) {
  const { token } = await props.params;
  try {
    const api = await createServerAxios();
    // Proxy call to backend public API history
    const { data } = await api.get(`/orders/track/${token}/history`);
    return NextResponse.json(data);
  } catch (err) {
    if (axios.isAxiosError(err))
      return NextResponse.json(err.response?.data ?? {}, { status: err.response?.status ?? 500 });
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}
