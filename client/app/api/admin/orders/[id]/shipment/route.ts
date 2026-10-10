import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { createServerAxios } from '@/lib/axios/serverAxios';

export async function POST(
  req: NextRequest,
  props: { params: Promise<{ id: string }> },
) {
  const { id } = await props.params;
  try {
    const api = await createServerAxios();
    const { data } = await api.post(`/internal/orders/${id}/shipment`);
    return NextResponse.json(data);
  } catch (error) {
    if (axios.isAxiosError(error)) {
      return NextResponse.json(error.response?.data ?? {}, { status: error.response?.status ?? 500 });
    }
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}

export async function DELETE(
  req: NextRequest,
  props: { params: Promise<{ id: string }> },
) {
  const { id } = await props.params;
  try {
    const api = await createServerAxios();
    const { data } = await api.delete(`/internal/orders/${id}/shipment`);
    return NextResponse.json(data);
  } catch (error) {
    if (axios.isAxiosError(error)) {
      return NextResponse.json(error.response?.data ?? {}, { status: error.response?.status ?? 500 });
    }
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}
