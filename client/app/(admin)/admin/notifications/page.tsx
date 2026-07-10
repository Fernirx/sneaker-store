import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import NotificationsClient from './_components/NotificationsClient';
import type { PageResult, NotificationResponse } from './_components/types';

export default async function AdminNotificationsPage() {
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get('/internal/notifications?page=0&size=20&sort=createdAt,desc');
    return <NotificationsClient initialData={data as PageResult<NotificationResponse>} />;
  } catch {
    redirect('/login');
  }
}
