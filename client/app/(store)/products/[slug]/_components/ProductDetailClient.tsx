'use client';

import { useState } from 'react';
import Link from 'next/link';
import { productUrl, brandUrl } from '@/lib/cloudinaryUrl';
import { type ProductDetailResponse, type SizeItem, formatPrice } from '../../_components/types';
import { useCart } from '@/contexts/CartContext';
import { parseApiError } from '@/lib/parseApiError';

const GENDER_LABEL: Record<string, string> = {
  MEN: 'Nam', WOMEN: 'Nữ', UNISEX: 'Unisex', KIDS: 'Trẻ em',
};

const CLOSURE_LABEL: Record<string, string> = {
  LACE: 'Buộc dây', SLIP_ON: 'Xỏ chân', VELCRO: 'Velcro', ZIPPER: 'Khóa kéo',
};

const SHAFT_LABEL: Record<string, string> = {
  LOW: 'Cổ thấp', MID: 'Cổ vừa', HIGH: 'Cổ cao',
};

export default function ProductDetailClient({ product }: { product: ProductDetailResponse }) {
  const { addItem } = useCart();

  const [colorIdx, setColorIdx]           = useState(0);
  const [selectedSize, setSelectedSize]   = useState<SizeItem | null>(null);
  const [mainImgIdx, setMainImgIdx]       = useState(0);
  const [qty, setQty]                     = useState(1);
  const [addedToCart, setAddedToCart]     = useState(false);
  const [addLoading, setAddLoading]       = useState(false);
  const [addError, setAddError]           = useState('');
  const [activeTab, setActiveTab]         = useState<'reviews' | 'description'>('reviews');

  const color         = product.colors[colorIdx];
  const currentImg    = color?.images[mainImgIdx] ?? color?.images[0];
  const hasMultiImgs  = (color?.images?.length ?? 0) > 1;

  const displayPrice  = product.basePrice;
  const hasDiscount   = product.originalPrice != null && product.originalPrice > displayPrice;
  const discountPct   = hasDiscount ? Math.round((1 - displayPrice / product.originalPrice!) * 100) : 0;

  function handleColorChange(idx: number) {
    setColorIdx(idx);
    setSelectedSize(null);
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

  // Build spec rows — only fields that exist; pad to even for 2-col grid
  const specRows: Array<{ k: string; v: string; mono?: boolean }> = [
    { k: "Giới tính",  v: GENDER_LABEL[product.gender] ?? product.gender },
    { k: "Thương hiệu",   v: product.brand.name },
    ...(product.upperMaterial ? [{ k: "Chất liệu mũi", v: product.upperMaterial }]                                  : []),
    ...(product.soleType      ? [{ k: "Loại đế",     v: product.soleType }]                                       : []),
    ...(product.closureType   ? [{ k: "Kiểu khóa",  v: CLOSURE_LABEL[product.closureType] ?? product.closureType }] : []),
    ...(product.shaftStyle    ? [{ k: "Kiểu cổ",    v: SHAFT_LABEL[product.shaftStyle] ?? product.shaftStyle }]     : []),
    ...(product.styleCode     ? [{ k: "Mã style",v: product.styleCode, mono: true }]                          : []),
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
                  className={`aspect-square border-[1.5px] rounded-sm overflow-hidden transition-all ${
                    i === mainImgIdx ? 'border-ink' : 'border-line hover:border-muted'
                  }`}
                >
                  <img
                    src={productUrl(img.publicId, 120, 120)}
                    alt=""
                    className="w-full h-full object-contain bg-paper p-1"
                  />
                </button>
              ))}
            </div>
          )}

          {/* Main image */}
          <div className="aspect-square bg-paper border-[1.5px] border-line rounded-lg overflow-hidden relative">
            {currentImg ? (
              <img
                src={productUrl(currentImg.publicId, 700, 700)}
                alt={product.name}
                className="w-full h-full object-contain p-5"
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-faint">—</div>
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
        <div className="space-y-5">

          {/* Brand */}
          <div>
            {product.brand.logoPublicId ? (
              <img
                src={brandUrl(product.brand.logoPublicId, 80, 40)}
                alt={product.brand.name}
                className="h-6 object-contain"
              />
            ) : (
              <span className="text-[11px] font-bold uppercase tracking-widest text-muted">
                {product.brand.name}
              </span>
            )}
          </div>

          {/* Name */}
          <h1 className="font-display font-black text-4xl leading-none tracking-tight">
            {product.name}
          </h1>

          {/* Price */}
          <div className="flex items-baseline gap-3 flex-wrap">
            <span className="font-display font-black text-[36px] leading-none tabular-nums">
              {formatPrice(displayPrice)}
            </span>
            {hasDiscount && (
              <>
                <span className="text-base text-faint line-through tabular-nums">
                  {formatPrice(product.originalPrice!)}
                </span>
                <span className="text-[10px] font-black text-white bg-accent px-2 py-1 rounded-sm tabular-nums">
                  -{discountPct}%
                </span>
              </>
            )}
          </div>

          {/* Color selector */}
          {product.colors.length > 0 && (
            <div>
              <p className="text-[13px] text-muted mb-2.5">
                {"Màu sắc"}
                {color && (
                  <span className="font-semibold text-ink"> / {color.colorway.toUpperCase()}</span>
                )}
              </p>
              <div className="flex gap-2.5 flex-wrap">
                {product.colors.map((c, i) => (
                  <button
                    key={c.colorway}
                    title={c.colorway}
                    onClick={() => handleColorChange(i)}
                    className={`w-8 h-8 rounded-full transition-all ${
                      i === colorIdx
                        ? 'ring-[0.5px] ring-ink ring-offset-[4px]'
                        : 'ring-[0.5px] ring-transparent ring-offset-[2px] hover:ring-line'
                    }`}
                    style={{ backgroundColor: c.colorHex ?? '#ccc' }}
                  />
                ))}
              </div>
            </div>
          )}

          {/* Size selector */}
          {color && color.sizes.length > 0 && (
            <div>
              <div className="flex justify-between items-center mb-2.5">
                <p className="text-[13px] text-muted">
                  {"Kích thước"} (EU)
                  {selectedSize && (
                    <span className="font-semibold text-ink"> / {selectedSize.size}</span>
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
              {selectedSize && selectedSize.stockQuantity > 0 && selectedSize.stockQuantity <= 5 && (
                <p className="text-[11.5px] text-warn mt-2.5">
                  ⚡ {`Chỉ còn ${selectedSize.stockQuantity} đôi size ${selectedSize.size} — đặt nhanh!`}
                </p>
              )}
            </div>
          )}

          {/* Qty stepper + CTA */}
          <div className="flex items-center gap-3">
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
              className="inline-flex items-center justify-center w-10 h-10 border-[1.5px] border-line rounded-sm bg-white text-ink hover:border-accent hover:text-accent transition-colors shrink-0"
              aria-label={"Thêm vào yêu thích"}
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 21C12 21 4 16 2 10.5C0.5 6.5 3.5 3.5 7 3.5C9 3.5 10.5 4.7 12 6C13.5 4.7 15 3.5 17 3.5C20.5 3.5 23.5 6.5 22 10.5C20 16 12 21 12 21Z"/>
              </svg>
            </button>
          </div>
          {addError && <p className="text-xs text-danger -mt-3">{addError}</p>}

          {/* Trust strip */}
          <div className="flex flex-wrap gap-6 py-4 border-t border-b border-line text-sm">
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

      {/* ── Tabs: Reviews + Description ── */}
      <section className="py-16">
        {/* Tab bar */}
        <div className="flex gap-1 border-b-[1.5px] border-line mb-7">
          {(['reviews', 'description'] as const).map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-4 py-[11px] font-display font-bold text-[13px] uppercase tracking-[0.03em] border-b-2 -mb-[1.5px] transition-colors ${
                activeTab === tab
                  ? 'text-ink border-accent'
                  : 'text-muted border-transparent hover:text-ink'
              }`}
            >
              {tab === 'reviews' ? "Đánh giá" : "Mô tả"}
            </button>
          ))}
        </div>

        {/* Reviews panel */}
        {activeTab === 'reviews' && (
          <div className="grid grid-cols-1 lg:grid-cols-[300px_1fr] gap-8 items-start">
            {/* Left: Rating summary */}
            <div>
              <div className="font-display font-black text-[64px] leading-none tracking-tight">—</div>
              <div className="text-[18px] text-faint mt-1.5 tracking-widest">★★★★★</div>
              <p className="text-[13px] text-muted mt-1">
                {"Chưa có đánh giá"}
              </p>

              {/* Rating bars */}
              <div className="space-y-2 mt-[18px]">
                {[5, 4, 3, 2, 1].map(star => (
                  <div key={star} className="flex items-center gap-2.5">
                    <span className="text-[12px] w-3.5 text-right shrink-0">{star}</span>
                    <div className="flex-1 h-[7px] bg-line-2 rounded-full overflow-hidden">
                      <div className="h-full bg-accent" style={{ width: '0%' }} />
                    </div>
                    <span className="text-[11px] text-muted w-7 shrink-0">0%</span>
                  </div>
                ))}
              </div>

              <button className="w-full mt-[18px] py-3 border-[1.5px] border-line rounded-sm bg-white text-sm font-bold hover:border-ink transition-colors">
                {"Viết đánh giá"}
              </button>
            </div>

            {/* Right: Empty state */}
            <div className="flex items-center justify-center py-16 text-sm text-muted border-[1.5px] border-line rounded-lg">
              {"Chưa có đánh giá nào."}
            </div>
          </div>
        )}

        {/* Description panel */}
        {activeTab === 'description' && (
          product.description
            ? <p className="text-sm text-muted leading-relaxed whitespace-pre-line max-w-2xl">{product.description}</p>
            : <p className="text-sm text-muted">{"Chưa có mô tả."}</p>
        )}
      </section>

    </div>
  );
}
