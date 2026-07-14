import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import DashboardClient from './_components/DashboardClient';
import type { DashboardSummary } from './_components/types';

export default async function DashboardPage() {
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get('/internal/dashboard/summary');
    return <DashboardClient initialData={data.data as DashboardSummary} />;
  } catch {
    redirect('/login');
  }
}
