'use client';

import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import clientAxios from '@/lib/axios/clientAxios';

export interface WishlistItemData {
  id: number;
  productId: number;
  productName: string;
  productSlug: string;
  brandName: string;
  primaryImagePublicId: string | null;
  variantId: number | null;
  colorway: string | null;
  colorHex: string | null;
  size: number | null;
  price: number;
  originalPrice: number | null;
  outOfStock: boolean;
  createdAt: string;
}

interface WishlistContextValue {
  items: WishlistItemData[];
  loading: boolean;
  isWishlisted: (productId: number, variantId: number | null) => WishlistItemData | undefined;
  add: (productId: number, variantId?: number | null) => Promise<WishlistItemData>;
  remove: (wishlistId: number) => Promise<void>;
}

const WishlistContext = createContext<WishlistContextValue | null>(null);

export function WishlistProvider({
  children,
  isLoggedIn,
}: {
  children: React.ReactNode;
  isLoggedIn: boolean;
}) {
  const [items, setItems] = useState<WishlistItemData[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isLoggedIn) {
      setItems([]);
      setLoading(false);
      return;
    }
    let cancelled = false;
    setLoading(true);
    clientAxios
      .get('/api/me/wishlist?page=0&size=200&sort=createdAt,desc')
      .then(({ data }) => { if (!cancelled) setItems(data.data ?? []); })
      .catch(() => { if (!cancelled) setItems([]); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [isLoggedIn]);

  const isWishlisted = useCallback(
    (productId: number, variantId: number | null) =>
      items.find(i => i.productId === productId && (i.variantId ?? null) === (variantId ?? null)),
    [items],
  );

  const add = useCallback(async (productId: number, variantId?: number | null) => {
    const { data } = await clientAxios.post('/api/me/wishlist', { productId, variantId: variantId ?? null });
    const item = data.data as WishlistItemData;
    setItems(prev => [item, ...prev]);
    return item;
  }, []);

  const remove = useCallback(async (wishlistId: number) => {
    await clientAxios.delete(`/api/me/wishlist/${wishlistId}`);
    setItems(prev => prev.filter(i => i.id !== wishlistId));
  }, []);

  return (
    <WishlistContext.Provider value={{ items, loading, isWishlisted, add, remove }}>
      {children}
    </WishlistContext.Provider>
  );
}

export function useWishlist(): WishlistContextValue {
  const ctx = useContext(WishlistContext);
  if (!ctx) throw new Error('useWishlist must be used within WishlistProvider');
  return ctx;
}