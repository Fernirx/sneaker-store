import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import StockAdjustmentDetailClient from './_components/StockAdjustmentDetailClient';
import type { StockAdjustmentResponse } from '../_components/types';

export default async function StockAdjustmentDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get(`/internal/stock-adjustments/${id}`);
    return (
      <StockAdjustmentDetailClient
        adjustment={data.data as StockAdjustmentResponse}
        isAdmin={session.roles.includes('ROLE_ADMIN')}
      />
    );
  } catch {
    redirect('/admin/stock-adjustments');
  }
}
