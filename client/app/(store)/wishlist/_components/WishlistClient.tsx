'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useWishlist, type WishlistItemData } from '@/contexts/WishlistContext';
import { useCart } from '@/contexts/CartContext';
import { productUrl } from '@/lib/cloudinaryUrl';
import { formatPrice } from '../../products/_components/types';
import { parseApiError } from '@/lib/parseApiError';

function WishlistCard({ item }: { item: WishlistItemData }) {
  const { remove } = useWishlist();
  const { addItem } = useCart();

  const [removing, setRemoving] = useState(false);
  const [adding, setAdding]     = useState(false);
  const [added, setAdded]       = useState(false);
  const [error, setError]       = useState('');

  const orig = item.originalPrice != null ? Number(item.originalPrice) : null;
  const price = Number(item.price);
  const pct = orig ? Math.round((1 - price / orig) * 100) : 0;

  async function handleRemove() {
    setRemoving(true);
    try { await remove(item.id); } catch { setRemoving(false); }
  }

  async function handleAddToCart() {
    if (!item.variantId) return;
    setAdding(true);
    setError('');
    try {
      await addItem(item.variantId, 1);
      setAdded(true);
      setTimeout(() => setAdded(false), 2000);
    } catch (err) {
      setError(parseApiError(err, "Không thể thêm vào giỏ hàng").general);
    } finally {
      setAdding(false);
    }
  }

  return (
    <div className={`flex gap-4 py-5 border-b border-line transition-opacity ${removing ? 'opacity-40 pointer-events-none' : ''}`}>
      {/* Image */}
      <Link
        href={`/products/${item.productSlug}`}
        className="shrink-0 w-[88px] h-[88px] rounded-sm border border-line bg-paper overflow-hidden"
      >
        {item.primaryImagePublicId ? (
          <img
            src={productUrl(item.primaryImagePublicId, 176, 176)}
            alt={item.productName}
            className="w-full h-full object-contain p-1.5"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-faint text-xs">—</div>
        )}
      </Link>

      {/* Info */}
      <div className="flex-1 min-w-0 flex flex-col justify-between gap-2">
        <div>
          <p className="text-[11px] font-bold uppercase tracking-widest text-muted">{item.brandName}</p>
          <Link
            href={`/products/${item.productSlug}`}
            className="text-[13px] font-semibold leading-snug text-ink hover:text-accent transition-colors line-clamp-2"
          >
            {item.productName}
          </Link>

          {(item.colorway || item.size) && (
            <p className="text-[12px] text-muted mt-1 leading-tight">
              {item.colorway}{item.colorway && item.size ? ' · ' : ''}{item.size ? `Size ${item.size}` : ''}
            </p>
          )}

          {item.outOfStock && (
            <span className="inline-block mt-1.5 text-[10px] font-black uppercase tracking-widest text-danger">
              {"Hết hàng"}
            </span>
          )}

          {error && <p className="text-xs text-danger mt-1">{error}</p>}
        </div>

        <div className="flex items-center gap-4">
          {item.variantId && !item.outOfStock && (
            <button
              onClick={handleAddToCart}
              disabled={adding}
              className={`text-[12px] font-bold uppercase tracking-wide transition-colors disabled:opacity-40 ${
                added ? 'text-ok' : 'text-ink hover:text-accent'
              }`}
            >
              {added ? `✓ ${"Đã thêm vào giỏ"}` : adding ? "Đang thêm..." : "Thêm vào giỏ hàng"}
            </button>
          )}
          <button
            onClick={handleRemove}
            disabled={removing}
            className="text-[12px] text-muted hover:text-danger transition-colors disabled:opacity-40"
          >
            {"Xóa"}
          </button>
        </div>
      </div>

      {/* Price */}
      <div className="shrink-0 flex flex-col items-end gap-0.5 pt-0.5 min-w-[80px]">
        {orig && (
          <span className="text-[10px] font-bold text-white bg-accent px-1.5 py-0.5 rounded-sm tabular-nums leading-none">
            -{pct}%
          </span>
        )}
        <span className={`text-[15px] font-bold tabular-nums leading-tight ${orig ? 'text-accent' : 'text-ink'}`}>
          {formatPrice(price)}
        </span>
        {orig && (
          <span className="text-[11px] text-faint line-through tabular-nums leading-none">
            {formatPrice(orig)}
          </span>
        )}
      </div>
    </div>
  );
}

function WishlistSkeleton() {
  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <div className="h-8 w-48 bg-line rounded-sm animate-pulse mb-8" />
      {[1, 2, 3].map(i => (
        <div key={i} className="flex gap-4 py-5 border-b border-line">
          <div className="w-[88px] h-[88px] bg-line rounded-sm animate-pulse shrink-0" />
          <div className="flex-1 space-y-2 pt-1">
            <div className="h-4 w-3/4 bg-line rounded-sm animate-pulse" />
            <div className="h-3 w-2/5 bg-line rounded-sm animate-pulse" />
          </div>
        </div>
      ))}
    </div>
  );
}

function EmptyState({ isLoggedIn }: { isLoggedIn: boolean }) {
  return (
    <div className="max-w-3xl mx-auto px-4 py-28 flex flex-col items-center gap-5">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.2" className="text-line">
        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
      </svg>
      <p className="text-muted text-[14px]">
        {isLoggedIn ? "Bạn chưa có sản phẩm yêu thích nào." : "Đăng nhập để xem danh sách yêu thích."}
      </p>
      <Link
        href={isLoggedIn ? '/products' : '/login'}
        className="text-[12px] font-bold uppercase tracking-widest bg-ink text-white px-6 py-3 rounded-sm hover:bg-accent transition-colors"
      >
        {isLoggedIn ? "Tiếp tục mua sắm" : "Đăng nhập"}
      </Link>
    </div>
  );
}

export default function WishlistClient({ isLoggedIn }: { isLoggedIn: boolean }) {
  const { items, loading } = useWishlist();

  if (!isLoggedIn) return <EmptyState isLoggedIn={false} />;
  if (loading) return <WishlistSkeleton />;
  if (items.length === 0) return <EmptyState isLoggedIn={true} />;

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <h1 className="font-display font-black text-3xl uppercase tracking-tight mb-1">
        {"Sản phẩm yêu thích"}
      </h1>
      <p className="text-[13px] text-muted mb-8">{`${items.length} sản phẩm`}</p>

      <div>
        {items.map(item => (
          <WishlistCard key={item.id} item={item} />
        ))}
      </div>
    </div>
  );
}
