import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';
import { revalidatePath } from 'next/cache';
import { createServerAxios } from '@/lib/axios/serverAxios';

export async function PATCH(
  req: NextRequest,
  props: { params: Promise<{ id: string }> },
) {
  const { id } = await props.params;
  try {
    const body = await req.json();
    const api = await createServerAxios();
    const { data } = await api.patch(`/me/addresses/${id}`, body);
    revalidatePath('/', 'layout');
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
  props: { params: Promise<{ id: string }> },
) {
  const { id } = await props.params;
  try {
    const api = await createServerAxios();
    await api.delete(`/me/addresses/${id}`);
    revalidatePath('/', 'layout');
    return NextResponse.json({ ok: true });
  } catch (error) {
    if (axios.isAxiosError(error)) {
      return NextResponse.json(error.response?.data ?? {}, { status: error.response?.status ?? 500 });
    }
    return NextResponse.json({ message: 'Lỗi máy chủ' }, { status: 500 });
  }
}
