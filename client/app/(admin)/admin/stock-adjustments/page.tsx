import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import StockAdjustmentsClient from './_components/StockAdjustmentsClient';
import type { PageData } from './_components/types';

export default async function StockAdjustmentsPage() {
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get('/internal/stock-adjustments?page=0&size=20&sort=createdAt,desc');
    return <StockAdjustmentsClient initialData={data as PageData} />;
  } catch {
    redirect('/login');
  }
}
