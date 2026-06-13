import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { createServerAxios } from '@/lib/axios/serverAxios';

export async function PATCH(
  req: NextRequest,
  props: { params: Promise<{ id: string; variantId: string }> },
) {
  const { id, variantId } = await props.params;
  try {
    const body = await req.json();
    const api = await createServerAxios();
    const { data } = await api.patch(`/internal/products/${id}/variants/${variantId}`, body);
    return NextResponse.json(data);
  } catch (error) {
    if (axios.isAxiosError(error)) {
      return NextResponse.json(error.response?.data ?? {}, { status: error.response?.status ?? 500 });
    }
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}

export async function DELETE(
  _req: NextRequest,
  props: { params: Promise<{ id: string; variantId: string }> },
) {
  const { id, variantId } = await props.params;
  try {
    const api = await createServerAxios();
    const { data } = await api.delete(`/internal/products/${id}/variants/${variantId}`);
    return NextResponse.json(data);
  } catch (error) {
    if (axios.isAxiosError(error)) {
      return NextResponse.json(error.response?.data ?? {}, { status: error.response?.status ?? 500 });
    }
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}
