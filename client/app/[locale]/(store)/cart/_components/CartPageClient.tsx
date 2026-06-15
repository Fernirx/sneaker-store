'use client';

import { useState, useTransition } from 'react';
import { useTranslations } from 'next-intl';
import { Link } from '@/i18n/routing';
import { useCart, type CartItemData } from '@/contexts/CartContext';
import { productUrl } from '@/lib/cloudinaryUrl';
import { formatPrice } from '../../products/_components/types';
import { parseApiError } from '@/lib/parseApiError';

function CartItemRow({ item }: { item: CartItemData }) {
  const t = useTranslations('cart');
  const { updateItem, removeItem } = useCart();
  const [qty, setQty] = useState(item.quantity);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState('');

  async function handleQty(next: number) {
    if (next < 1 || next === qty) return;
    setError('');
    setUpdating(true);
    try {
      setQty(next);
      await updateItem(item.id, next);
    } catch (err) {
      const { general } = parseApiError(err, 'Lỗi cập nhật');
      setError(general);
      setQty(item.quantity);
    } finally {
      setUpdating(false);
    }
  }

  async function handleRemove() {
    setUpdating(true);
    try {
      await removeItem(item.id);
    } catch {
      setUpdating(false);
    }
  }

  return (
    <div className={`flex gap-4 py-5 border-b border-line transition-opacity ${updating ? 'opacity-50' : ''}`}>
      {/* Image */}
      <Link href={`/products/${item.productSlug}`} className="shrink-0">
        <div className="w-20 h-20 border border-line rounded-sm overflow-hidden bg-paper">
          {item.primaryImagePublicId ? (
            <img
              src={productUrl(item.primaryImagePublicId, 160, 160)}
              alt={item.productName}
              className="w-full h-full object-contain p-1"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-muted text-xs">—</div>
          )}
        </div>
      </Link>

      {/* Info */}
      <div className="flex-1 min-w-0">
        <Link href={`/products/${item.productSlug}`}
          className="font-semibold text-sm text-ink hover:text-accent transition-colors line-clamp-2">
          {item.productName}
        </Link>
        <p className="text-xs text-muted mt-1">
          {t('color')}: {item.colorway} · {t('size')}: {item.size}
        </p>
        {item.outOfStock && (
          <span className="inline-block mt-1 text-[10px] font-bold uppercase text-danger bg-danger-bg px-1.5 py-0.5 rounded-sm">
            {t('outOfStock')}
          </span>
        )}
        {error && <p className="text-xs text-danger mt-1">{error}</p>}
      </div>

      {/* Price + qty + remove */}
      <div className="flex flex-col items-end gap-2 shrink-0">
        <span className="font-bold text-sm">
          {formatPrice(item.unitPrice * qty)}
        </span>

        {/* Quantity controls */}
        <div className="flex items-center border border-line rounded-sm overflow-hidden">
          <button
            onClick={() => handleQty(qty - 1)}
            disabled={updating || qty <= 1}
            className="w-8 h-8 flex items-center justify-center text-ink-2 hover:bg-paper disabled:opacity-30 transition-colors text-lg leading-none"
          >
            −
          </button>
          <span className="w-8 text-center text-sm font-bold">{qty}</span>
          <button
            onClick={() => handleQty(qty + 1)}
            disabled={updating || qty >= item.stockQuantity}
            className="w-8 h-8 flex items-center justify-center text-ink-2 hover:bg-paper disabled:opacity-30 transition-colors text-lg leading-none"
          >
            +
          </button>
        </div>

        <button
          onClick={handleRemove}
          disabled={updating}
          className="text-xs text-muted hover:text-danger transition-colors disabled:opacity-40"
        >
          {t('remove')}
        </button>
      </div>
    </div>
  );
}

export default function CartPageClient() {
  const t = useTranslations('cart');
  const { cart, loading, clearCart } = useCart();
  const [clearing, startClearing] = useTransition();

  function handleClear() {
    startClearing(async () => { await clearCart().catch(() => {}); });
  }

  if (loading) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-16 text-center text-muted text-sm">
        <div className="animate-pulse space-y-4">
          {[1, 2, 3].map(i => (
            <div key={i} className="h-24 bg-paper rounded-sm" />
          ))}
        </div>
      </div>
    );
  }

  const items = cart?.items ?? [];

  if (items.length === 0) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-24 flex flex-col items-center gap-6">
        <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.2" className="text-muted">
          <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/>
        </svg>
        <p className="text-muted text-base">{t('empty')}</p>
        <Link href="/products"
          className="font-display font-bold text-sm uppercase tracking-wider bg-ink text-white px-6 py-3 rounded-sm hover:bg-accent transition-colors">
          {t('continueShopping')}
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      {/* Title */}
      <h1 className="font-display font-black text-2xl uppercase tracking-tight mb-1">
        {t('title')}
      </h1>
      <p className="text-xs text-muted mb-8">
        {t('totalItems', { count: cart?.totalItems ?? 0 })}
      </p>

      {/* Items */}
      <div>
        {items.map(item => (
          <CartItemRow key={item.id} item={item} />
        ))}
      </div>

      {/* Footer */}
      <div className="mt-8 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-6">
        <button
          onClick={handleClear}
          disabled={clearing}
          className="text-sm text-muted hover:text-danger transition-colors disabled:opacity-40 text-left"
        >
          {clearing ? t('clearing') : t('clearCart')}
        </button>

        <div className="flex flex-col items-end gap-4">
          <div className="text-right">
            <p className="text-xs text-muted uppercase tracking-wider">{t('total')}</p>
            <p className="font-display font-black text-2xl mt-0.5">
              {formatPrice(cart?.totalAmount ?? 0)}
            </p>
          </div>
          <button
            className="w-full sm:w-auto font-display font-black text-sm uppercase tracking-wider bg-ink text-white px-8 py-4 rounded-sm hover:bg-accent transition-colors"
          >
            {t('checkout')}
          </button>
        </div>
      </div>
    </div>
  );
}
