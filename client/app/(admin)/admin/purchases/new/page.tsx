import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import NewPurchaseClient from './_components/NewPurchaseClient';
import type { SupplierBrief } from '../_components/types';

export default async function NewPurchasePage() {
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get('/internal/suppliers?page=0&size=200&sort=name,asc&active=true');
    return <NewPurchaseClient suppliers={data.data as SupplierBrief[]} />;
  } catch {
    redirect('/login');
  }
}
