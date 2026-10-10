import { getSession } from '@/lib/session';
import CheckoutClient from './_components/CheckoutClient';

export default async function CheckoutPage() {
  const session = await getSession();
  return <CheckoutClient isLoggedIn={!!session} userEmail={session?.email} />;
}
