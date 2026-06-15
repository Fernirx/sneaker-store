'use client';

import { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import { createPortal } from 'react-dom';
import { useTranslations } from 'next-intl';
import clientAxios from '@/lib/axios/clientAxios';

export interface CartItemData {
  id: number;
  variantId: number;
  quantity: number;
  previousQuantity?: number | null;
  productName: string;
  productSlug: string;
  primaryImagePublicId: string | null;
  colorway: string;
  size: number;
  unitPrice: number;
  originalPrice?: number | null;
  selected: boolean;
  stockQuantity: number;
  outOfStock: boolean;
}

export interface CartData {
  guestToken: string | null;
  items: CartItemData[];
  totalItems: number;
  totalAmount: number;
}

interface CartAdjustment {
  productName: string;
  from: number;
  to: number;
}

interface CartContextValue {
  cart: CartData | null;
  loading: boolean;
  addItem: (variantId: number, quantity?: number) => Promise<void>;
  updateItem: (itemId: number, quantity: number) => Promise<void>;
  removeItem: (itemId: number) => Promise<void>;
  toggleSelection: (itemId: number, selected: boolean) => Promise<void>;
  clearCart: () => Promise<void>;
}

const CartContext = createContext<CartContextValue | null>(null);

const GUEST_TOKEN_KEY = 'guest_cart_token';
const EMPTY_CART: CartData = { guestToken: null, items: [], totalItems: 0, totalAmount: 0 };

function getGuestToken(): string | null {
  try { return localStorage.getItem(GUEST_TOKEN_KEY); } catch { return null; }
}
function saveGuestToken(token: string | null) {
  try { if (token) localStorage.setItem(GUEST_TOKEN_KEY, token); } catch { /* ignore */ }
}
function clearGuestToken() {
  try { localStorage.removeItem(GUEST_TOKEN_KEY); } catch { /* ignore */ }
}
function guestHeaders(): Record<string, string> {
  const t = getGuestToken();
  return t ? { 'X-Guest-Token': t } : {};
}

function extractAdjustments(data: CartData): CartAdjustment[] {
  return (data.items ?? [])
    .filter(i => i.previousQuantity != null)
    .map(i => ({ productName: i.productName, from: i.previousQuantity!, to: i.quantity }));
}

// ---- Toast component ----

function AdjustmentToast({
  adjustments,
  onDismiss,
  getMsg,
}: {
  adjustments: CartAdjustment[];
  onDismiss: (idx: number) => void;
  getMsg: (adj: CartAdjustment) => string;
}) {
  return (
    <div className="fixed top-6 right-6 z-50 flex flex-col gap-2 max-w-sm pointer-events-none">
      {adjustments.map((adj, i) => (
        <div
          key={i}
          className="pointer-events-auto bg-ink text-white text-sm px-4 py-3 rounded-sm shadow-xl flex items-start gap-3 animate-toast-in"
        >
          <svg
            className="shrink-0 mt-0.5 text-yellow-400"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>

          <p className="flex-1 text-xs leading-relaxed">
            {getMsg(adj)}
          </p>

          <button
            onClick={() => onDismiss(i)}
            className="shrink-0 text-white/50 hover:text-white transition-colors leading-none text-base"
          >
            ×
          </button>
        </div>
      ))}
    </div>
  );
}

// ---- Provider ----

export function CartProvider({
  children,
  isLoggedIn,
}: {
  children: React.ReactNode;
  isLoggedIn: boolean;
}) {
  const t = useTranslations('cart');
  const [cart, setCart] = useState<CartData | null>(null);
  const [loading, setLoading] = useState(true);
  const [adjustments, setAdjustments] = useState<CartAdjustment[]>([]);
  const [mounted, setMounted] = useState(false);
  // Track cart object identity to avoid showing the same adjustments twice
  const lastCartRef = useRef<CartData | null>(null);

  useEffect(() => { setMounted(true); }, []);

  const applyCart = useCallback((data: CartData) => {
    setCart(data);
    if (data.guestToken) saveGuestToken(data.guestToken);
  }, []);

  // Detect stock-adjustment toasts whenever cart state changes
  useEffect(() => {
    if (!cart || cart === lastCartRef.current) return;
    lastCartRef.current = cart;
    const adj = extractAdjustments(cart);
    if (adj.length > 0) setAdjustments(prev => [...prev, ...adj]);
  }, [cart]);

  // Auto-dismiss toasts after 6s
  useEffect(() => {
    if (adjustments.length === 0) return;
    const timer = setTimeout(() => setAdjustments([]), 6000);
    return () => clearTimeout(timer);
  }, [adjustments]);

  function dismissOne(idx: number) {
    setAdjustments(prev => prev.filter((_, i) => i !== idx));
  }

  useEffect(() => {
    async function init() {
      setLoading(true);
      try {
        if (isLoggedIn) {
          const guestToken = getGuestToken();
          if (guestToken) {
            const { data: res } = await clientAxios.post('/api/cart/merge', { guestToken });
            applyCart(res.data);
            clearGuestToken();
          } else {
            const { data: res } = await clientAxios.get('/api/cart');
            applyCart(res.data);
          }
        } else {
          const headers = guestHeaders();
          if (Object.keys(headers).length > 0) {
            const { data: res } = await clientAxios.get('/api/cart', { headers });
            applyCart(res.data);
          } else {
            setCart(EMPTY_CART);
          }
        }
      } catch {
        setCart(EMPTY_CART);
      } finally {
        setLoading(false);
      }
    }
    init();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isLoggedIn]);

  const addItem = useCallback(async (variantId: number, quantity = 1) => {
    const { data: res } = await clientAxios.post(
      '/api/cart/items',
      { variantId, quantity },
      { headers: guestHeaders() },
    );
    applyCart(res.data);
  }, [applyCart]);

  const updateItem = useCallback(async (itemId: number, quantity: number) => {
    const { data: res } = await clientAxios.patch(
      `/api/cart/items/${itemId}`,
      { quantity },
      { headers: guestHeaders() },
    );
    applyCart(res.data);
  }, [applyCart]);

  const removeItem = useCallback(async (itemId: number) => {
    const { data: res } = await clientAxios.delete(`/api/cart/items/${itemId}`, {
      headers: guestHeaders(),
    });
    applyCart(res.data);
  }, [applyCart]);

  const toggleSelection = useCallback(async (itemId: number, selected: boolean) => {
    const { data: res } = await clientAxios.patch(
      `/api/cart/items/${itemId}/selection`,
      { selected },
      { headers: guestHeaders() },
    );
    applyCart(res.data);
  }, [applyCart]);

  const clearCart = useCallback(async () => {
    const { data: res } = await clientAxios.delete('/api/cart', { headers: guestHeaders() });
    applyCart(res.data);
  }, [applyCart]);

  return (
    <CartContext.Provider value={{ cart, loading, addItem, updateItem, removeItem, toggleSelection, clearCart }}>
      {children}
      {mounted && adjustments.length > 0 && createPortal(
        <AdjustmentToast
          adjustments={adjustments}
          onDismiss={dismissOne}
          getMsg={(adj) => t('adjusted', { name: adj.productName, from: adj.from, to: adj.to })}
        />,
        document.body,
      )}
    </CartContext.Provider>
  );
}

export function useCart(): CartContextValue {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart must be used within CartProvider');
  return ctx;
}
