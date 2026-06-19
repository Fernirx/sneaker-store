import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import PurchasesClient from './_components/PurchasesClient';
import type { PageData, SupplierBrief } from './_components/types';

export default async function PurchasesPage() {
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const [purchasesRes, suppliersRes] = await Promise.all([
      api.get('/internal/purchases?page=0&size=20&sort=createdAt,desc'),
      api.get('/internal/suppliers?page=0&size=200&sort=name,asc'),
    ]);
    return (
      <PurchasesClient
        initialData={purchasesRes.data as PageData}
        suppliers={suppliersRes.data.data as SupplierBrief[]}
      />
    );
  } catch {
    redirect('/login');
  }
}
