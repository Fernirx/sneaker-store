import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import MarketingListClient from './_components/MarketingListClient';
import type { PageResult, NotificationInternalResponse } from '../_components/types';

export default async function MarketingNotificationsPage() {
  const session = await getSession();
  if (!session) redirect('/login');
  if (!session.roles.includes('ROLE_ADMIN')) {
    redirect('/admin/notifications');
  }

  try {
    const api = await createServerAxios();
    const { data } = await api.get('/internal/notifications/marketing?page=0&size=20&sort=createdAt,desc');
    return <MarketingListClient initialData={data as PageResult<NotificationInternalResponse>} />;
  } catch {
    redirect('/login');
  }
}
