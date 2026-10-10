import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { createServerAxios } from '@/lib/axios/serverAxios';

type Params = { params: Promise<{ itemId: string }> };

function guestHeader(req: NextRequest): Record<string, string> {
  const t = req.headers.get('X-Guest-Token');
  return t ? { 'X-Guest-Token': t } : {};
}

export async function PATCH(req: NextRequest, { params }: Params) {
  try {
    const { itemId } = await params;
    const body = await req.json();
    const api = await createServerAxios();
    const { data } = await api.patch(`/cart/items/${itemId}/selection`, body, {
      headers: guestHeader(req),
    });
    return NextResponse.json(data);
  } catch (err) {
    if (axios.isAxiosError(err))
      return NextResponse.json(err.response?.data ?? {}, { status: err.response?.status ?? 500 });
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}
