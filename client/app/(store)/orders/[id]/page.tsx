import { getSession } from '@/lib/session';
import OrderDetailClient from './_components/OrderDetailClient';

type SearchParams = Promise<{ [key: string]: string | string[] | undefined }>;

export default async function OrderDetailPage({
  params,
  searchParams,
}: {
  params: Promise<{ id: string }>;
  searchParams: SearchParams;
}) {
  const { id } = await params;
  const sp = await searchParams;
  const session = await getSession();
  const orderTokenFromUrl = typeof sp.orderToken === 'string' ? sp.orderToken : null;
  return (
    <OrderDetailClient
      orderId={Number(id)}
      isLoggedIn={!!session}
      orderTokenFromUrl={orderTokenFromUrl}
    />
  );
}
