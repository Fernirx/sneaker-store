import { getSession } from '@/lib/session';
import NotificationsClient from './_components/NotificationsClient';

export default async function NotificationsPage() {
  const session = await getSession();
  return <NotificationsClient isLoggedIn={!!session} />;
}
