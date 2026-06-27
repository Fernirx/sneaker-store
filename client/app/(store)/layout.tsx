import Header from '@/components/Header';
import Footer from '@/components/Footer';
import WishlistDrawer from '@/components/WishlistDrawer';
import { CartProvider } from '@/contexts/CartContext';
import { WishlistProvider } from '@/contexts/WishlistContext';
import { getSession } from '@/lib/session';

export default async function StoreLayout({ children }: { children: React.ReactNode }) {
  const session = await getSession();
  return (
    <CartProvider isLoggedIn={!!session}>
      <WishlistProvider isLoggedIn={!!session}>
        <Header />
        {children}
        <Footer />
        <WishlistDrawer />
      </WishlistProvider>
    </CartProvider>
  );
}
