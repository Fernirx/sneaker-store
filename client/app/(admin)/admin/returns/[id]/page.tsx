import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import ReturnDetailClient from './_components/ReturnDetailClient';
import type { ReturnRequestInternalResponse } from '../_components/types';

export default async function ReturnDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get(`/internal/returns/${id}`);
    return (
      <ReturnDetailClient
        returnRequest={data.data as ReturnRequestInternalResponse}
        canApprove={session.roles.includes('ROLE_ADMIN') || session.roles.includes('ROLE_SALE')}
        canWarehouse={session.roles.includes('ROLE_ADMIN') || session.roles.includes('ROLE_WAREHOUSE')}
      />
    );
  } catch {
    redirect('/admin/returns');
  }
}
