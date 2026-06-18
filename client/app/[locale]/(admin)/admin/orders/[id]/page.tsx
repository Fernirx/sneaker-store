import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import OrderDetailClient from './_components/OrderDetailClient';
import type { OrderInternalResponse } from '../_components/types';

export default async function OrderDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get(`/internal/orders/${id}`);
    return <OrderDetailClient order={data.data as OrderInternalResponse} />;
  } catch {
    redirect('/admin/orders');
  }
}
