'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useWishlist, type WishlistItemData } from '@/contexts/WishlistContext';
import { useCart } from '@/contexts/CartContext';
import { productUrl } from '@/lib/cloudinaryUrl';
import { formatPrice } from '@/app/(store)/products/_components/types';
import { parseApiError } from '@/lib/parseApiError';
import ImageUnavailable from '@/components/ImageUnavailable';

function DrawerItem({ item, onClose }: { item: WishlistItemData; onClose: () => void }) {
  const { remove } = useWishlist();
  const { addItem } = useCart();

  const [removing, setRemoving] = useState(false);
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);
  const [error, setError] = useState('');

  const price = Number(item.price);
  const orig = item.originalPrice != null ? Number(item.originalPrice) : null;

  async function handleRemove() {
    setRemoving(true);
    try {
      await remove(item.id);
    } catch {
      setRemoving(false);
    }
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
    <div className={`flex flex-col relative transition-opacity ${removing ? 'opacity-40 pointer-events-none' : ''}`}>
      {/* Thumbnail */}
      <div className="relative aspect-square w-full bg-transparent overflow-hidden group/thumb mb-2">
        <Link
          href={`/products/${item.productSlug}`}
          onClick={onClose}
          className="w-full h-full block"
        >
          {item.primaryImagePublicId ? (
            <img
              src={productUrl(item.primaryImagePublicId, 300, 300)}
              alt={item.productName}
              className="w-full h-full object-contain group-hover/thumb:scale-105 transition-transform duration-300"
            />
          ) : (
            <ImageUnavailable className="w-8 h-8" />
          )}
        </Link>

        {/* Remove button */}
        <button
          onClick={handleRemove}
          disabled={removing}
          title="Xóa khỏi yêu thích"
          className="absolute top-1 right-1 z-10 w-8 h-8 rounded-full bg-white/90 hover:bg-white text-[#FF4081] flex items-center justify-center shadow-sm hover:scale-110 transition-all cursor-pointer"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="18"
            height="18"
            viewBox="0 0 24 24"
            role="presentation"
            fill="none"
            className="text-[#FF4081]"
          >
            <path
              d="M12 5.64507C10.9383 4.62619 9.49681 4 7.90909 4C4.64559 4 2 6.64559 2 9.90909C2 15.6984 8.33668 18.8955 10.9216 19.9588C11.6178 20.2452 12.3822 20.2452 13.0784 19.9588C15.6633 18.8955 22 15.6984 22 9.90909C22 6.64559 19.3544 4 16.0909 4C14.5032 4 13.0617 4.62619 12 5.64507Z"
              fill="currentColor"
            />
          </svg>
        </button>
      </div>

      {/* Details */}
      <div className="flex flex-col flex-1 justify-between gap-2 mt-1">
        <div className="flex flex-col gap-1">
          {/* Product Name */}
          <Link
            href={`/products/${item.productSlug}`}
            onClick={onClose}
            className="text-xs font-bold leading-snug text-ink hover:text-accent transition-colors line-clamp-2"
          >
            {item.productName}
          </Link>

          {/* Price */}
          <div className="flex items-baseline gap-1.5">
            <span className="font-display font-bold text-xs text-ink">{formatPrice(price)}</span>
            {orig != null && orig > price && (
              <span className="text-[10px] text-muted line-through">{formatPrice(orig)}</span>
            )}
          </div>

          {/* Color and Size */}
          <div className="flex items-center gap-3 text-[11px] text-muted leading-tight">
            <span className="truncate">Màu sắc: {item.colorway || '—'}</span>
            <span className="shrink-0 font-medium">Size: {item.size || '—'}</span>
          </div>

          {item.outOfStock && (
            <span className="inline-block text-[10px] font-black uppercase tracking-widest text-danger">
              Hết hàng
            </span>
          )}

          {error && <p className="text-[10px] text-danger">{error}</p>}
        </div>

        {/* Action button */}
        <div className="mt-1">
          {item.variantId && !item.outOfStock ? (
            <button
              onClick={handleAddToCart}
              disabled={adding}
              className={`w-full text-center text-xs font-bold py-1.5 px-3 rounded-full border transition-all cursor-pointer disabled:opacity-40 ${
                added ? 'border-ok text-ok bg-transparent' : 'border-line hover:border-ink text-ink bg-transparent'
              }`}
            >
              {added ? "✓ Đã thêm" : adding ? "Đang thêm..." : "Thêm vào giỏ hàng"}
            </button>
          ) : (
            <Link
              href={`/products/${item.productSlug}`}
              onClick={onClose}
              className="w-full block text-center text-xs font-bold py-1.5 px-3 rounded-full border border-line hover:border-ink text-ink bg-transparent transition-colors"
            >
              Chọn phiên bản →
            </Link>
          )}
        </div>
      </div>
    </div>
  );
}

export default function WishlistDrawer() {
  const [isOpen, setIsOpen] = useState(false);
  const { items, loading } = useWishlist();
  const count = items.length;

  useEffect(() => {
    function handleOpen() {
      setIsOpen(true);
    }
    window.addEventListener('open-wishlist-drawer', handleOpen);
    return () => window.removeEventListener('open-wishlist-drawer', handleOpen);
  }, []);

  return (
    <>
      {/* Floating button */}
      <button
        onClick={() => setIsOpen(true)}
        className="fixed right-0 top-1/2 -translate-y-1/2 z-40 bg-white/95 text-ink hover:text-accent rounded-l-2xl rounded-r-none py-3.5 px-2 flex flex-col items-center gap-2.5 transition-colors cursor-pointer group"
        aria-label="Open Danh sách yêu thích sidebar"
        title="Danh sách yêu thích"
      >
        {/* Border mask */}
        <div
          className="absolute inset-0 rounded-l-2xl rounded-r-none pointer-events-none"
          style={{
            border: '1px solid rgba(0, 0, 0, 0.08)',
            maskImage: 'linear-gradient(to right, black 0%, black 50%, transparent 100%)',
            WebkitMaskImage: 'linear-gradient(to right, black 0%, black 50%, transparent 100%)'
          }}
        />

        {/* Label */}
        <span
          style={{ writingMode: 'vertical-rl', transform: 'rotate(180deg)' }}
          className="font-display font-bold text-xs uppercase tracking-wider select-none pt-1 relative z-10"
        >
          Danh sách yêu thích
        </span>

        {/* Heart icon */}
        <div className="w-8 h-8 rounded-full bg-ink text-white group-hover:bg-accent flex items-center justify-center transition-colors shrink-0 relative z-10">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            role="presentation"
            fill="none"
            className={count > 0 ? "text-accent group-hover:text-white" : "text-white"}
          >
            <path
              d="M12 5.64507C10.9383 4.62619 9.49681 4 7.90909 4C4.64559 4 2 6.64559 2 9.90909C2 15.6984 8.33668 18.8955 10.9216 19.9588C11.6178 20.2452 12.3822 20.2452 13.0784 19.9588C15.6633 18.8955 22 15.6984 22 9.90909C22 6.64559 19.3544 4 16.0909 4C14.5032 4 13.0617 4.62619 12 5.64507Z"
              fill="currentColor"
            />
          </svg>
        </div>
      </button>

      {/* Backdrop */}
      <div
        className={`fixed inset-0 bg-black/50 backdrop-blur-xs z-50 transition-opacity duration-300 ${
          isOpen ? 'opacity-100 pointer-events-auto' : 'opacity-0 pointer-events-none'
        }`}
        onClick={() => setIsOpen(false)}
      />

      {/* Slide-over Drawer */}
      <div
        className={`fixed inset-y-0 right-0 max-w-full w-full sm:w-[440px] bg-white shadow-2xl z-50 flex flex-col transform transition-transform duration-300 ease-in-out ${
          isOpen ? 'translate-x-0' : 'translate-x-full'
        }`}
      >
        {/* Drawer Header */}
        <div className="px-5 py-4 border-b border-line flex items-center justify-between bg-paper">
          <h2 className="font-display font-bold text-base text-ink">
            Danh sách yêu thích của tôi ({count})
          </h2>
          <button
            onClick={() => setIsOpen(false)}
            className="w-8 h-8 rounded-full flex items-center justify-center text-muted hover:text-ink hover:bg-line/40 transition-colors"
          >
            ✕
          </button>
        </div>

        {/* Drawer Content */}
        <div className="flex-1 overflow-y-auto px-5">
          {loading ? (
            <div className="py-12 text-center text-sm text-muted">Đang tải...</div>
          ) : count === 0 ? (
            <div className="py-16 flex flex-col items-center justify-center text-center">
              <div className="w-16 h-16 rounded-full bg-paper flex items-center justify-center text-faint mb-4">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
              </div>
              <p className="font-bold text-sm text-ink mb-1">Chưa có sản phẩm yêu thích</p>
              <p className="text-xs text-muted max-w-[220px] leading-relaxed mb-6">
                Hãy bấm vào biểu tượng trái tim trên sản phẩm bạn thích để lưu vào đây nhé!
              </p>
              <Link
                href="/"
                onClick={() => setIsOpen(false)}
                className="font-display font-bold text-xs uppercase tracking-wider bg-ink text-white px-5 py-2.5 rounded-sm hover:bg-accent transition-colors"
              >
                Khám phá ngay
              </Link>
            </div>
          ) : (
            <div className="grid grid-cols-2 gap-x-3.5 gap-y-6 py-5">
              {items.map(item => <DrawerItem key={item.id} item={item} onClose={() => setIsOpen(false)} />)}
            </div>
          )}
        </div>
      </div>
    </>
  );
}
