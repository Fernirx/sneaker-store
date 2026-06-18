import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { createServerAxios } from '@/lib/axios/serverAxios';

function guestHeader(req: NextRequest): Record<string, string> {
  const t = req.headers.get('X-Guest-Token');
  return t ? { 'X-Guest-Token': t } : {};
}

export async function GET(
  req: NextRequest,
  props: { params: Promise<{ id: string }> },
) {
  const { id } = await props.params;
  try {
    const api = await createServerAxios();
    const { data } = await api.get(`/orders/${id}/history`, { headers: guestHeader(req) });
    return NextResponse.json(data);
  } catch (err) {
    if (axios.isAxiosError(err))
      return NextResponse.json(err.response?.data ?? {}, { status: err.response?.status ?? 500 });
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}
