import { getSession } from '@/lib/session';
import OrdersClient from './_components/OrdersClient';

export default async function OrdersPage() {
  const session = await getSession();
  return <OrdersClient isLoggedIn={!!session} />;
}
