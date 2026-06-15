'use client';

import { useState, useTransition } from 'react';
import { useTranslations } from 'next-intl';
import { Link } from '@/i18n/routing';
import { useCart, type CartItemData } from '@/contexts/CartContext';
import { productUrl } from '@/lib/cloudinaryUrl';
import { formatPrice } from '../../products/_components/types';
import { parseApiError } from '@/lib/parseApiError';


// ── Item row ──────────────────────────────────────────────────────────────────

function CartItemRow({ item }: { item: CartItemData }) {
  const t = useTranslations('cart');
  const { updateItem, removeItem, toggleSelection } = useCart();
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

  async function handleToggleSelection(selected: boolean) {
    setUpdating(true);
    try {
      await toggleSelection(item.id, selected);
    } catch {
      // ignore
    } finally {
      setUpdating(false);
    }
  }

  const lowStock = !item.outOfStock && item.stockQuantity > 0 && item.stockQuantity <= 3;
  const lineTotal = Number(item.unitPrice) * qty;

  return (
    <div className={`grid grid-cols-[20px_96px_1fr_auto] gap-3 py-5 border-b border-line transition-opacity ${updating ? 'opacity-40 pointer-events-none' : ''} ${!item.selected ? 'opacity-50' : ''}`}>
      {/* Checkbox */}
      <div className="flex items-start pt-1.5">
        <input
          type="checkbox"
          checked={item.selected}
          onChange={(e) => handleToggleSelection(e.target.checked)}
          className="w-4 h-4 rounded-sm cursor-pointer accent-accent"
        />
      </div>

      {/* Image */}
      <Link href={`/products/${item.productSlug}`} className="block w-24 h-24 rounded-sm overflow-hidden border border-line bg-paper shrink-0">
        {item.primaryImagePublicId ? (
          <img
            src={productUrl(item.primaryImagePublicId, 192, 192)}
            alt={item.productName}
            className="w-full h-full object-contain p-1.5"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-faint text-xs">—</div>
        )}
      </Link>

      {/* Info */}
      <div className="min-w-0">
        <Link
          href={`/products/${item.productSlug}`}
          className="font-semibold text-sm leading-snug text-ink hover:text-accent transition-colors line-clamp-2"
        >
          {item.productName}
        </Link>

        {/* Attribute pills */}
        <div className="flex flex-wrap gap-1.5 mt-2">
          <span className="inline-flex items-center text-[11px] font-bold text-ink border border-ink/20 bg-white rounded-sm px-2 py-0.5 tracking-wide">
            {t('size')} {item.size}
          </span>
          <span className="inline-flex items-center text-[11px] text-muted bg-paper border border-line rounded-sm px-2 py-0.5">
            {item.colorway}
          </span>
        </div>

        {/* Stock badges */}
        <div className="flex flex-wrap gap-1.5 mt-1.5">
          {item.outOfStock && (
            <span className="inline-flex items-center text-[10px] font-black uppercase tracking-widest text-white bg-danger px-2 py-0.5 rounded-sm">
              {t('outOfStock')}
            </span>
          )}
          {lowStock && (
            <span className="inline-flex items-center gap-1 text-[10px] font-black uppercase tracking-widest text-warn px-2 py-0.5 rounded-sm border border-warn/30 bg-warn-bg">
              <svg width="9" height="9" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2a10 10 0 1 0 0 20A10 10 0 0 0 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
              {t('lowStock', { n: item.stockQuantity })}
            </span>
          )}
        </div>

        {error && <p className="text-xs text-danger mt-1">{error}</p>}

        {/* Controls */}
        <div className="flex items-center gap-3 mt-3">
          {/* Stepper */}
          <div className="inline-flex items-center h-8 rounded-sm border border-line overflow-hidden divide-x divide-line bg-white">
            <button
              onClick={() => handleQty(qty - 1)}
              disabled={updating || qty <= 1}
              className="w-8 h-8 flex items-center justify-center text-ink hover:bg-paper disabled:opacity-25 disabled:cursor-not-allowed transition-colors text-base leading-none select-none"
            >
              −
            </button>
            <span className="w-9 h-8 flex items-center justify-center text-sm font-bold tabular-nums text-ink">{qty}</span>
            <button
              onClick={() => handleQty(qty + 1)}
              disabled={updating || item.outOfStock || qty >= item.stockQuantity}
              className="w-8 h-8 flex items-center justify-center text-ink hover:bg-paper disabled:opacity-25 disabled:cursor-not-allowed transition-colors text-base leading-none select-none"
            >
              +
            </button>
          </div>

          {/* Remove */}
          <button
            onClick={handleRemove}
            disabled={updating}
            className="inline-flex items-center gap-1 text-[11px] text-faint hover:text-danger transition-colors disabled:opacity-40"
          >
            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
              <path d="M18 6L6 18M6 6l12 12"/>
            </svg>
            {t('remove')}
          </button>
        </div>
      </div>

      {/* Price */}
      {(() => {
        const orig = item.originalPrice ? Number(item.originalPrice) : null;
        const unit = Number(item.unitPrice);
        const discountPct = orig ? Math.round((1 - unit / orig) * 100) : 0;
        return (
          <div className="flex flex-col items-end gap-0.5 shrink-0 pt-0.5 min-w-[90px]">
            {orig && (
              <>
                <span className="text-[10px] font-black text-white bg-accent px-1.5 py-0.5 rounded-sm tabular-nums leading-tight">
                  -{discountPct}%
                </span>
                <span className="text-xs text-faint line-through tabular-nums font-mono leading-tight">
                  {formatPrice(orig * qty)}
                </span>
              </>
            )}
            <span className={`font-display font-black text-base leading-tight tabular-nums ${orig ? 'text-accent' : 'text-ink'}`}>
              {formatPrice(unit * qty)}
            </span>
            {qty > 1 && (
              <span className="text-[11px] text-faint font-mono tabular-nums leading-tight">
                {formatPrice(unit)} × {qty}
              </span>
            )}
          </div>
        );
      })()}
    </div>
  );
}

// ── Order summary sidebar ─────────────────────────────────────────────────────

function OrderSummary({
  items,
  totalAmount,
  onCheckout,
  onClear,
  clearing,
}: {
  items: CartItemData[];
  totalAmount: number;
  onCheckout: () => void;
  onClear: () => void;
  clearing: boolean;
}) {
  const t = useTranslations('cart');

  const selectedItems = items.filter(i => i.selected);
  const subtotalOriginal = selectedItems.reduce((sum, i) => {
    const price = i.originalPrice ? Number(i.originalPrice) : Number(i.unitPrice);
    return sum + price * i.quantity;
  }, 0);
  const totalDiscount = subtotalOriginal - totalAmount;
  const hasOutOfStockSelected = selectedItems.some(i => i.outOfStock);

  return (
    <aside className="lg:sticky lg:top-[84px]">
      <div className="border border-line rounded-sm overflow-hidden bg-white">
        {/* Header */}
        <div className="px-5 py-4 border-b border-line">
          <h2 className="font-display font-black text-sm uppercase tracking-wider">
            {t('orderSummary')}
          </h2>
        </div>

        {/* Body */}
        <div className="px-5 py-5 space-y-3">
          {/* Subtotal at original price */}
          <div className="flex justify-between items-baseline text-sm">
            <span className="text-muted">{t('subtotal')}</span>
            <span className="tabular-nums font-mono">{formatPrice(subtotalOriginal)}</span>
          </div>

          {/* Discount row — only shown when there is a discount */}
          {totalDiscount > 0 && (
            <div className="flex justify-between items-baseline text-sm">
              <span className="text-muted">{t('discount')}</span>
              <span className="tabular-nums font-mono text-ok font-semibold">
                -{formatPrice(totalDiscount)}
              </span>
            </div>
          )}

          {/* Divider + total */}
          <div className="border-t border-line pt-3">
            <div className="flex justify-between items-baseline">
              <span className="font-display font-black text-sm uppercase tracking-wider">
                {t('total')}
              </span>
              <span className="font-display font-black text-2xl tabular-nums">
                {formatPrice(totalAmount)}
              </span>
            </div>
          </div>

          {/* Checkout */}
          <button
            onClick={onCheckout}
            disabled={hasOutOfStockSelected || selectedItems.length === 0}
            className="w-full font-display font-black text-sm uppercase tracking-wider bg-accent text-white py-4 rounded-sm hover:bg-accent-700 transition-colors disabled:opacity-40 disabled:cursor-not-allowed disabled:hover:bg-accent"
          >
            {t('checkout')}
          </button>
          {hasOutOfStockSelected && (
            <p className="text-[11px] text-danger text-center leading-tight">
              {t('checkoutBlockedByOutOfStock')}
            </p>
          )}

          {/* Payment badges */}
          <div className="flex justify-center gap-2 pt-1">
            {['VNPAY', 'COD', 'GHN'].map(m => (
              <span
                key={m}
                className="text-[10px] text-muted bg-paper border border-line rounded-sm px-2 py-0.5 font-mono"
              >
                {m}
              </span>
            ))}
          </div>
        </div>
      </div>

      {/* Clear cart */}
      <button
        onClick={onClear}
        disabled={clearing}
        className="w-full text-xs text-muted hover:text-danger transition-colors disabled:opacity-40 mt-4 text-center"
      >
        {clearing ? t('clearing') : t('clearCart')}
      </button>
    </aside>
  );
}

// ── Loading skeleton ──────────────────────────────────────────────────────────

function CartSkeleton() {
  return (
    <div className="max-w-6xl mx-auto px-4 py-10">
      <div className="h-9 w-40 bg-line rounded-sm mb-2 animate-pulse" />
      <div className="h-4 w-28 bg-line rounded-sm mb-10 animate-pulse" />
      <div className="grid grid-cols-1 lg:grid-cols-[1fr_360px] gap-8 items-start">
        <div className="space-y-0">
          {[1, 2, 3].map(i => (
            <div key={i} className="grid grid-cols-[96px_1fr_auto] gap-4 py-5 border-b border-line">
              <div className="w-24 h-24 bg-line rounded-sm animate-pulse" />
              <div className="space-y-2 pt-1">
                <div className="h-4 w-3/4 bg-line rounded-sm animate-pulse" />
                <div className="h-3 w-2/5 bg-line rounded-sm animate-pulse" />
                <div className="h-8 w-24 bg-line rounded-sm animate-pulse mt-4" />
              </div>
              <div className="w-20 h-5 bg-line rounded-sm animate-pulse" />
            </div>
          ))}
        </div>
        <div className="h-64 bg-line rounded-sm animate-pulse" />
      </div>
    </div>
  );
}

// ── Page ──────────────────────────────────────────────────────────────────────

export default function CartPageClient() {
  const t = useTranslations('cart');
  const { cart, loading, clearCart } = useCart();
  const [clearing, startClearing] = useTransition();

  function handleClear() {
    startClearing(async () => { await clearCart().catch(() => {}); });
  }

  if (loading) return <CartSkeleton />;

  const items = cart?.items ?? [];

  if (items.length === 0) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-24 flex flex-col items-center gap-6">
        <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.2" className="text-faint">
          <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
          <line x1="3" y1="6" x2="21" y2="6"/>
          <path d="M16 10a4 4 0 0 1-8 0"/>
        </svg>
        <p className="text-muted text-base">{t('empty')}</p>
        <Link
          href="/products"
          className="font-display font-black text-sm uppercase tracking-wider bg-ink text-white px-6 py-3 rounded-sm hover:bg-accent transition-colors"
        >
          {t('continueShopping')}
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-4 py-10">
      <h1 className="font-display font-black text-4xl uppercase tracking-tight mb-1">
        {t('title')}
      </h1>
      <p className="text-sm text-muted mb-10">
        {t('totalItems', { count: cart?.totalItems ?? 0 })}
      </p>

      <div className="grid grid-cols-1 lg:grid-cols-[1fr_360px] gap-8 items-start">
        {/* Item list */}
        <section>
          {items.map(item => (
            <CartItemRow key={item.id} item={item} />
          ))}

          <Link
            href="/products"
            className="inline-flex items-center gap-2 mt-6 text-sm font-semibold text-accent hover:underline underline-offset-2"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M19 12H5M11 6l-6 6 6 6"/>
            </svg>
            {t('continueShopping')}
          </Link>
        </section>

        {/* Summary */}
        <OrderSummary
          items={items}
          totalAmount={Number(cart?.totalAmount ?? 0)}
          onCheckout={() => {}}
          onClear={handleClear}
          clearing={clearing}
        />
      </div>
    </div>
  );
}
