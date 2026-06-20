import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import PurchaseDetailClient from './_components/PurchaseDetailClient';
import type { PurchaseResponse, SupplierBrief } from '../_components/types';

export default async function PurchaseDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const [purchaseRes, suppliersRes] = await Promise.all([
      api.get(`/internal/purchases/${id}`),
      api.get('/internal/suppliers?page=0&size=200&sort=name,asc'),
    ]);
    return (
      <PurchaseDetailClient
        purchase={purchaseRes.data.data as PurchaseResponse}
        suppliers={suppliersRes.data.data as SupplierBrief[]}
      />
    );
  } catch {
    redirect('/admin/purchases');
  }
}
