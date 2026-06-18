import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import OrdersClient from './_components/OrdersClient';
import type { PageResult } from './_components/types';

export default async function OrdersPage() {
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get('/internal/orders?page=0&size=20&sort=createdAt,desc');
    return <OrdersClient initialData={data as PageResult} />;
  } catch {
    redirect('/login');
  }
}
