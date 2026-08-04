'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { productUrl, brandUrl } from '@/lib/cloudinaryUrl';
import { type ProductDetailResponse, type SizeItem, formatPrice } from '../../_components/types';
import { useCart } from '@/contexts/CartContext';
import { useWishlist } from '@/contexts/WishlistContext';
import { parseApiError } from '@/lib/parseApiError';
import ReviewsPanel from './ReviewsPanel';
import CommentsPanel from './CommentsPanel';
import ImageUnavailable from '@/components/ImageUnavailable';
import RichText from '@/components/RichText';

const GENDER_LABEL: Record<string, string> = {
  MEN: 'Nam', WOMEN: 'Nữ', UNISEX: 'Unisex', KIDS: 'Trẻ em',
};

const CLOSURE_LABEL: Record<string, string> = {
  LACE: 'Buộc dây', SLIP_ON: 'Xỏ chân', VELCRO: 'Velcro', ZIPPER: 'Khóa kéo',
};

const SHAFT_LABEL: Record<string, string> = {
  LOW: 'Cổ thấp', MID: 'Cổ vừa', HIGH: 'Cổ cao',
};

export default function ProductDetailClient({
  product,
  isLoggedIn,
  currentUserId,
}: {
  product: ProductDetailResponse;
  isLoggedIn: boolean;
  currentUserId: number | null;
}) {
  const { addItem } = useCart();
  const { isWishlisted, add: addWishlist, remove: removeWishlist } = useWishlist();
  const router = useRouter();

  const [colorIdx, setColorIdx]           = useState(0);
  const [selectedSize, setSelectedSize]   = useState<SizeItem | null>(() => {
    const initColor = product.colors[0];
    return initColor?.sizes?.find(s => s.stockQuantity > 0) ?? initColor?.sizes?.[0] ?? null;
  });
  const [mainImgIdx, setMainImgIdx]       = useState(0);
  const [qty, setQty]                     = useState(1);
  const [addedToCart, setAddedToCart]     = useState(false);
  const [addLoading, setAddLoading]       = useState(false);
  const [addError, setAddError]           = useState('');
  const [wishlistPending, setWishlistPending] = useState(false);
  const [activeTab, setActiveTab]         = useState<'reviews' | 'qa'>('reviews');

  const color         = product.colors[colorIdx];
  const currentImg    = color?.images[mainImgIdx] ?? color?.images[0];
  const hasMultiImgs  = (color?.images?.length ?? 0) > 1;

  const currentVariantId = selectedSize?.variantId ?? null;
  const wishlistEntry    = isWishlisted(product.id, currentVariantId);

  const displayPrice  = selectedSize?.price ?? color?.sizes?.[0]?.price ?? product.minPrice;
  const originalPrice = selectedSize?.originalPrice ?? color?.sizes?.[0]?.originalPrice ?? null;
  const hasDiscount   = originalPrice != null && originalPrice > (displayPrice ?? 0);
  const discountPct   = hasDiscount ? Math.round(((originalPrice! - displayPrice!) / originalPrice!) * 100) : 0;

  function handleColorChange(idx: number) {
    setColorIdx(idx);
    const targetColor = product.colors[idx];
    const targetSize  = targetColor?.sizes?.find(s => s.stockQuantity > 0) ?? targetColor?.sizes?.[0] ?? null;
    setSelectedSize(targetSize);
    setMainImgIdx(0);
    setQty(1);
  }

  async function handleAddToCart() {
    if (!selectedSize || addLoading) return;
    setAddError('');
    setAddLoading(true);
    try {
      await addItem(selectedSize.variantId, qty);
      setAddedToCart(true);
      setTimeout(() => setAddedToCart(false), 2000);
    } catch (err) {
      const { general } = parseApiError(err, "Không thể thêm vào giỏ hàng");
      setAddError(general);
    } finally {
      setAddLoading(false);
    }
  }

  async function handleToggleWishlist() {
    if (!isLoggedIn) { router.push('/login'); return; }
    if (wishlistPending) return;
    setWishlistPending(true);
    try {
      if (wishlistEntry) await removeWishlist(wishlistEntry.id);
      else await addWishlist(product.id, currentVariantId);
    } catch {
      // ignore — heart state simply won't change
    } finally {
      setWishlistPending(false);
    }
  }

  // Build spec rows — only fields that exist; pad to even for 2-col grid
  const specRows: Array<{ k: string; v: string; mono?: boolean }> = [
    { k: "Giới tính",  v: GENDER_LABEL[product.gender] ?? product.gender },
    { k: "Thương hiệu",   v: product.brand.name },
    ...(product.upperMaterial ? [{ k: "Chất liệu mũi", v: product.upperMaterial }]                                  : []),
    ...(product.soleType      ? [{ k: "Loại đế",     v: product.soleType }]                                       : []),
    ...(product.closureType   ? [{ k: "Kiểu khóa",  v: CLOSURE_LABEL[product.closureType] ?? product.closureType }] : []),
    ...(product.shaftStyle    ? [{ k: "Kiểu cổ",    v: SHAFT_LABEL[product.shaftStyle] ?? product.shaftStyle }]     : []),
    { k: "Đã bán", v: `${product.soldCount.toLocaleString('vi-VN')} ${"đôi"}` },
  ];
  if (specRows.length % 2 !== 0) specRows.push({ k: '', v: '' });

  return (
    <div className="max-w-7xl mx-auto px-6 py-6">
      {/* Breadcrumb */}
      <nav className="text-[12.5px] text-muted mb-5 flex items-center gap-2 flex-wrap">
        <Link href="/" className="hover:text-ink transition-colors">{"Trang chủ"}</Link>
        <span className="text-faint">/</span>
        <Link href="/products" className="hover:text-ink transition-colors">{"Sản phẩm"}</Link>
        <span className="text-faint">/</span>
        <span className="text-ink">{product.name}</span>
      </nav>

      <div className="grid grid-cols-1 md:grid-cols-[1.1fr_0.9fr] gap-[44px] items-start">

        {/* ── LEFT: Gallery ── */}
        <div
          className={`md:sticky md:top-[84px] ${
            hasMultiImgs
              ? 'grid grid-cols-[60px_1fr] md:grid-cols-[76px_1fr] gap-3.5'
              : ''
          }`}
        >
          {/* Thumbnails */}
          {hasMultiImgs && (
            <div className="flex flex-col gap-2">
              {color.images.map((img, i) => (
                <button
                  key={img.publicId}
                  onClick={() => setMainImgIdx(i)}
                  className={`aspect-square rounded-sm overflow-hidden transition-all ${
                    i === mainImgIdx ? 'border-[1.5px] border-ink bg-transparent' : 'border-[1.5px] border-transparent hover:opacity-80 bg-transparent'
                  }`}
                >
                  <img
                    src={productUrl(img.publicId, 120, 120)}
                    alt=""
                    className="w-full h-full object-contain bg-transparent"
                  />
                </button>
              ))}
            </div>
          )}

          {/* Main image */}
          <div className="aspect-square bg-transparent rounded-lg overflow-hidden relative">
            {currentImg ? (
              <img
                src={productUrl(currentImg.publicId, 700, 700)}
                alt={product.name}
                className="w-full h-full object-contain"
              />
            ) : (
              <ImageUnavailable className="w-14 h-14" />
            )}

            {/* Badges */}
            <div className="absolute top-3 left-3 flex flex-col gap-1.5">
              {product.newArrival && (
                <span className="bg-ink text-white text-[9px] font-black uppercase tracking-widest px-2 py-1 rounded-sm">
                  {"MỚI"}
                </span>
              )}
              {product.onSale && (
                <span className="bg-accent text-white text-[9px] font-black uppercase tracking-widest px-2 py-1 rounded-sm">
                  {"SALE"}
                </span>
              )}
            </div>
          </div>
        </div>

        {/* ── RIGHT: Info ── */}
        <div className="flex flex-col">

          {/* Name */}
          <h1 className="font-display font-black text-3xl leading-tight tracking-tight text-ink">
            {product.name}
          </h1>

          {/* Brand & SKU */}
          <div className="flex flex-col gap-0.5 -mt-1.5">
            <div>
              {product.brand.logoPublicId ? (
                <img
                  src={brandUrl(product.brand.logoPublicId, 80, 40)}
                  alt={product.brand.name}
                  className="h-4 object-contain"
                />
              ) : (
                <span className="text-xs font-normal uppercase tracking-widest text-muted">
                  {product.brand.name}
                </span>
              )}
            </div>
            {selectedSize?.sku && (
              <span className="text-xs font-normal uppercase tracking-widest text-muted">
                SKU: {selectedSize.sku}
              </span>
            )}
          </div>

          {/* Price */}
          <div className="flex items-baseline gap-3 flex-wrap mt-5">
            {displayPrice != null ? (
              <span className="font-display font-normal text-2xl leading-none tabular-nums text-ink">
                {(!selectedSize && product.maxPrice != null && product.maxPrice !== product.minPrice) 
                  ? `${formatPrice(product.minPrice!)} - ${formatPrice(product.maxPrice)}`
                  : formatPrice(displayPrice)}
              </span>
            ) : (
              <span className="text-lg text-muted font-normal">Chưa có giá</span>
            )}
            {hasDiscount && (
              <>
                <span className="text-base text-faint line-through tabular-nums">
                  {formatPrice(originalPrice!)}
                </span>
                <span className="text-[10px] font-black text-white bg-accent px-2 py-1 rounded-sm tabular-nums">
                  -{discountPct}%
                </span>
              </>
            )}
          </div>

          {/* Trust strip */}
          <div className="flex flex-wrap gap-6 py-4 border-t border-b border-line text-sm mt-4">
            <span className="flex items-center gap-2 text-ok">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M20 6L9 17l-5-5"/>
              </svg>
              {"Chính hãng 100%"}
            </span>
            <span className="flex items-center gap-2 text-ok">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M3 12a9 9 0 1 0 9-9"/><path d="M3 4v5h5"/>
              </svg>
              {"Đổi trả 30 ngày"}
            </span>
          </div>

          {/* Color selector */}
          {product.colors.length > 0 && (
            <div className="mt-6">
              <p className="text-[13px] text-muted mb-2.5">
                {"Màu Sắc:"}
                {color && (
                  <span className="font-semibold text-ink"> {color.colorway.toUpperCase()}</span>
                )}
              </p>
              <div className="flex gap-2.5 flex-wrap">
                {product.colors.map((c, i) => (
                  <button
                    key={c.colorway}
                    title={c.colorway}
                    onClick={() => handleColorChange(i)}
                    className={`w-8 h-8 rounded-full border border-black/25 transition-all duration-200 cursor-pointer ${
                      i === colorIdx
                        ? 'ring-2 ring-ink ring-offset-2 scale-110 shadow-md border-black/40'
                        : 'hover:scale-105 hover:border-black/50'
                    }`}
                    style={{ backgroundColor: c.colorHex ?? '#ccc' }}
                  />
                ))}
              </div>
            </div>
          )}

          {/* Size selector */}
          {color && color.sizes.length > 0 && (
            <div className="mt-4">
              <div className="flex justify-between items-center mb-2.5">
                <p className="text-[13px] text-muted">
                  {"Kích thước(Size):"}
                  {selectedSize && (
                    <span className="font-semibold text-ink"> {selectedSize.size}</span>
                  )}
                </p>
                <span className="text-xs text-muted underline cursor-pointer">
                  {"Hướng dẫn chọn size"}
                </span>
              </div>
              <div className="grid grid-cols-4 gap-2">
                {color.sizes.map(s => {
                  const outOfStock = s.stockQuantity === 0;
                  const isSelected = selectedSize?.variantId === s.variantId;
                  return (
                    <button
                      key={s.variantId}
                      disabled={outOfStock}
                      onClick={() => { setSelectedSize(s); setQty(1); }}
                      className={`py-3 text-center text-sm tabular-nums transition-all ${
                        outOfStock
                          ? 'border border-line bg-line-2 text-faint line-through cursor-not-allowed'
                          : isSelected
                          ? 'border-2 border-ink font-semibold text-ink bg-white'
                          : 'border border-line bg-white text-ink hover:border-muted'
                      }`}
                    >
                      {s.size}
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          {/* Qty label, Stepper, Remaining Stock */}
          <div className="mt-5">
            <p className="text-[13px] text-muted mb-2">{"Số lượng:"}</p>
            <div>
              <div className="inline-flex items-center border-[1.5px] border-line rounded-sm overflow-hidden shrink-0">
                <button
                  onClick={() => setQty(q => Math.max(1, q - 1))}
                  disabled={qty <= 1}
                  className="w-9 h-[38px] bg-white text-base text-ink hover:bg-line-2 disabled:opacity-25 disabled:cursor-not-allowed transition-colors select-none"
                >
                  −
                </button>
                <span className="w-[42px] text-center font-semibold tabular-nums text-ink border-x border-[1.5px] border-line h-[38px] flex items-center justify-center">
                  {qty}
                </span>
                <button
                  onClick={() => setQty(q => selectedSize ? Math.min(q + 1, selectedSize.stockQuantity) : q + 1)}
                  disabled={!selectedSize || qty >= selectedSize.stockQuantity}
                  className="w-9 h-[38px] bg-white text-base text-ink hover:bg-line-2 disabled:opacity-25 disabled:cursor-not-allowed transition-colors select-none"
                >
                  +
                </button>
              </div>
            </div>

            {selectedSize && (
              <p className={`text-xs mt-2.5 ${selectedSize.stockQuantity < 5 ? 'text-danger font-semibold' : 'text-muted'}`}>
                {`Còn ${selectedSize.stockQuantity} sản phẩm`}
              </p>
            )}
          </div>

          {/* CTA & Wishlist */}
          <div className="mt-4">
            <div className="flex gap-3">
              <button
                onClick={handleAddToCart}
                disabled={!selectedSize || addLoading}
                className={`flex-1 py-4 font-display font-black text-sm uppercase tracking-wider rounded-sm transition-all ${
                  addedToCart
                    ? 'bg-ok text-white'
                    : selectedSize && !addLoading
                    ? 'bg-ink text-white hover:bg-accent'
                    : 'bg-paper text-muted border border-line cursor-not-allowed'
                }`}
              >
                {addedToCart
                  ? `✓ ${"Đã thêm vào giỏ"}`
                  : addLoading
                  ? '...'
                  : !selectedSize
                  ? "Chọn size"
                  : "Thêm vào giỏ hàng"}
              </button>

              {/* Wishlist */}
              <button
                onClick={handleToggleWishlist}
                disabled={wishlistPending}
                className={`inline-flex items-center justify-center w-11 border-[1.5px] rounded-sm transition-colors shrink-0 disabled:opacity-50 ${
                  wishlistEntry
                    ? 'border-accent bg-accent text-white hover:bg-accent-700'
                    : 'border-line bg-white text-ink hover:border-accent hover:text-accent'
                }`}
                aria-label={wishlistEntry ? "Xóa khỏi yêu thích" : "Thêm vào yêu thích"}
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill={wishlistEntry ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 21C12 21 4 16 2 10.5C0.5 6.5 3.5 3.5 7 3.5C9 3.5 10.5 4.7 12 6C13.5 4.7 15 3.5 17 3.5C20.5 3.5 23.5 6.5 22 10.5C20 16 12 21 12 21Z"/>
                </svg>
              </button>
            </div>
            {addError && <p className="text-xs text-danger mt-2">{addError}</p>}

            <p className="text-base font-normal text-ink text-center my-4">
              {"Vui lòng chọn màu trước khi mua"}
            </p>
          </div>

          {/* Specs grid */}
          <div>
            <p className="text-[11px] font-bold uppercase tracking-widest text-muted mb-3">
              {"Thông tin sản phẩm"}
            </p>
            <div className="grid grid-cols-2 border-l border-t border-line rounded-sm overflow-hidden">
              {specRows.map((spec, i) => (
                <div key={i} className="border-r border-b border-line px-4 py-3">
                  {spec.k && (
                    <>
                      <div className="text-[10.5px] uppercase tracking-widest text-muted">
                        {spec.k}
                      </div>
                      <div className="font-semibold text-sm mt-0.5">
                        {spec.v}
                      </div>
                    </>
                  )}
                </div>
              ))}
            </div>
          </div>

        </div>
      </div>

      {/* ── Dedicated Description Section ── */}
      {product.description && (
        <section className="py-12 border-t border-line mt-10">
          <h2 className="font-display font-black text-base uppercase tracking-wider text-ink mb-4">Mô tả sản phẩm</h2>
          <RichText html={product.description} className="text-sm text-muted leading-relaxed max-w-3xl" />
        </section>
      )}

      {/* ── Tabs: Reviews + Q&A ── */}
      <section className="py-12 border-t border-line">
        {/* Tab bar */}
        <div className="flex gap-1 border-b-[1.5px] border-line mb-7">
          {(['reviews', 'qa'] as const).map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-4 py-[11px] font-display font-bold text-[13px] uppercase tracking-[0.03em] border-b-2 -mb-[1.5px] transition-colors ${
                activeTab === tab
                  ? 'text-ink border-accent'
                  : 'text-muted border-transparent hover:text-ink'
              }`}
            >
              {tab === 'reviews' ? "Đánh giá" : "Hỏi & đáp"}
            </button>
          ))}
        </div>

        {/* Reviews panel */}
        {activeTab === 'reviews' && (
          <ReviewsPanel
            slug={product.slug}
            productId={product.id}
            isLoggedIn={isLoggedIn}
            currentUserId={currentUserId}
          />
        )}

        {/* Q&A panel */}
        {activeTab === 'qa' && (
          <CommentsPanel
            slug={product.slug}
            productId={product.id}
            isLoggedIn={isLoggedIn}
            currentUserId={currentUserId}
          />
        )}
      </section>

    </div>
  );
}
