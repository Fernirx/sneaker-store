import { getSession } from '@/lib/session';
import WishlistClient from './_components/WishlistClient';

export default async function WishlistPage() {
  const session = await getSession();
  return <WishlistClient isLoggedIn={!!session} />;
}
