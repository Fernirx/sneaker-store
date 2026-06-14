'use client';

import { useState } from 'react';
import { useTranslations, useLocale } from 'next-intl';
import { Link } from '@/i18n/routing';
import { productUrl, brandUrl } from '@/lib/cloudinaryUrl';
import { type ProductDetailResponse, formatPrice } from '../../_components/types';

const GENDER_LABEL: Record<string, { vi: string; en: string }> = {
  MEN:    { vi: 'Nam',     en: 'Men' },
  WOMEN:  { vi: 'Nữ',     en: 'Women' },
  UNISEX: { vi: 'Unisex', en: 'Unisex' },
  KIDS:   { vi: 'Trẻ em', en: 'Kids' },
};

const CLOSURE_LABEL: Record<string, { vi: string; en: string }> = {
  LACE:    { vi: 'Buộc dây', en: 'Lace-up' },
  LACE_UP: { vi: 'Buộc dây', en: 'Lace-up' },
  SLIP_ON: { vi: 'Xỏ chân',  en: 'Slip-on' },
  VELCRO:  { vi: 'Velcro',   en: 'Velcro' },
  ZIPPER:  { vi: 'Khóa kéo', en: 'Zipper' },
};

const SHAFT_LABEL: Record<string, { vi: string; en: string }> = {
  LOW:      { vi: 'Cổ thấp', en: 'Low-top' },
  LOW_TOP:  { vi: 'Cổ thấp', en: 'Low-top' },
  MID:      { vi: 'Cổ vừa',  en: 'Mid-top' },
  MID_TOP:  { vi: 'Cổ vừa',  en: 'Mid-top' },
  HIGH:     { vi: 'Cổ cao',  en: 'High-top' },
  HIGH_TOP: { vi: 'Cổ cao',  en: 'High-top' },
};

function label(map: Record<string, { vi: string; en: string }>, key: string | null | undefined, locale: string) {
  if (!key) return null;
  const entry = map[key];
  if (!entry) return key;
  return locale === 'vi' ? entry.vi : entry.en;
}

export default function ProductDetailClient({ product }: { product: ProductDetailResponse }) {
  const t = useTranslations('products');
  const locale = useLocale();

  const [colorIdx, setColorIdx] = useState(0);
  const [selectedVariantId, setSelectedVariantId] = useState<number | null>(null);
  const [mainImgIdx, setMainImgIdx] = useState(0);
  const [addedToCart, setAddedToCart] = useState(false);

  const color = product.colors[colorIdx];
  const primaryImg = color?.images.find(i => i.primary) ?? color?.images[0];
  const currentMainImg = color?.images[mainImgIdx] ?? primaryImg;
  const displayPrice = product.basePrice;

  function handleColorChange(idx: number) {
    setColorIdx(idx);
    setSelectedVariantId(null);
    setMainImgIdx(0);
  }

  function handleAddToCart() {
    if (!selectedVariantId) return;
    setAddedToCart(true);
    setTimeout(() => setAddedToCart(false), 2000);
  }

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      {/* Breadcrumb */}
      <nav className="text-xs text-muted mb-6 flex items-center gap-1.5">
        <Link href="/" className="hover:text-ink transition-colors">Home</Link>
        <span>/</span>
        <Link href="/products" className="hover:text-ink transition-colors">{t('title')}</Link>
        <span>/</span>
        <span className="text-ink font-medium line-clamp-1">{product.name}</span>
      </nav>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-10 lg:gap-16">
        {/* Left: Images */}
        <div className="space-y-3">
          {/* Main image */}
          <div className="aspect-square bg-paper border border-line rounded-sm overflow-hidden">
            {currentMainImg ? (
              <img
                src={productUrl(currentMainImg.publicId, 700, 700)}
                alt={product.name}
                className="w-full h-full object-contain p-4"
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-muted">—</div>
            )}
          </div>

          {/* Thumbnails */}
          {color && color.images.length > 1 && (
            <div className="flex gap-2 flex-wrap">
              {color.images.map((img, i) => (
                <button
                  key={img.publicId}
                  onClick={() => setMainImgIdx(i)}
                  className={`w-16 h-16 border-2 rounded-sm overflow-hidden flex-shrink-0 transition-all ${
                    i === mainImgIdx ? 'border-ink' : 'border-line hover:border-muted'
                  }`}
                >
                  <img
                    src={productUrl(img.publicId, 128, 128)}
                    alt=""
                    className="w-full h-full object-contain bg-paper"
                  />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Right: Info */}
        <div className="space-y-6">
          {/* Brand + badges */}
          <div className="flex items-center gap-2">
            {product.brand.logoPublicId ? (
              <img
                src={brandUrl(product.brand.logoPublicId, 80, 40)}
                alt={product.brand.name}
                className="h-6 object-contain"
              />
            ) : (
              <span className="text-[11px] font-bold uppercase tracking-wider text-muted">
                {product.brand.name}
              </span>
            )}
            {product.newArrival && (
              <span className="bg-ink text-white text-[9px] font-bold px-2 py-0.5 rounded-sm uppercase">
                {t('newBadge')}
              </span>
            )}
            {product.onSale && (
              <span className="bg-accent text-white text-[9px] font-bold px-2 py-0.5 rounded-sm uppercase">
                {t('saleBadge')}
              </span>
            )}
          </div>

          {/* Name */}
          <h1 className="font-display font-black text-2xl leading-tight">{product.name}</h1>

          {/* Price */}
          <div className="flex items-baseline gap-3">
            <span className="font-display font-black text-2xl">{formatPrice(displayPrice)}</span>
            {product.originalPrice != null && product.originalPrice > displayPrice && (
              <span className="text-base text-muted line-through">{formatPrice(product.originalPrice)}</span>
            )}
          </div>

          {/* Color selector */}
          {product.colors.length > 0 && (
            <div>
              <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-2">
                {t('chooseColor')}
                {color && (
                  <span className="ml-2 font-normal normal-case tracking-normal text-ink">
                    {color.colorway}
                  </span>
                )}
              </p>
              <div className="flex gap-2 flex-wrap">
                {product.colors.map((c, i) => (
                  <button
                    key={c.colorway}
                    title={c.colorway}
                    onClick={() => handleColorChange(i)}
                    className={`w-8 h-8 rounded-full border-2 transition-all ${
                      i === colorIdx
                        ? 'ring-2 ring-ink ring-offset-2'
                        : 'hover:ring-2 hover:ring-muted hover:ring-offset-1'
                    }`}
                    style={{
                      backgroundColor: c.colorHex ?? '#ccc',
                      borderColor: c.colorHex ?? '#ccc',
                    }}
                  />
                ))}
              </div>
            </div>
          )}

          {/* Size selector */}
          {color && color.sizes.length > 0 && (
            <div>
              <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-2">
                {t('selectSize')}
              </p>
              <div className="flex gap-2 flex-wrap">
                {color.sizes.map(s => {
                  const outOfStock = s.stockQuantity === 0;
                  const selected = selectedVariantId === s.variantId;
                  return (
                    <button
                      key={s.variantId}
                      disabled={outOfStock}
                      onClick={() => setSelectedVariantId(s.variantId)}
                      className={`min-w-[48px] px-3 py-2 border rounded-sm text-sm font-bold transition-all ${
                        outOfStock
                          ? 'border-line text-line cursor-not-allowed line-through'
                          : selected
                          ? 'border-ink bg-ink text-white'
                          : 'border-line text-ink hover:border-ink'
                      }`}
                    >
                      {s.size}
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          {/* Add to cart */}
          <button
            onClick={handleAddToCart}
            disabled={!selectedVariantId}
            className={`w-full py-4 font-display font-black text-sm uppercase tracking-wider rounded-sm transition-all ${
              addedToCart
                ? 'bg-ok text-white'
                : selectedVariantId
                ? 'bg-ink text-white hover:bg-accent'
                : 'bg-paper text-muted border border-line cursor-not-allowed'
            }`}
          >
            {addedToCart
              ? '✓ Đã thêm vào giỏ'
              : !selectedVariantId
              ? t('selectSize')
              : t('addToCart')}
          </button>

          {/* Product details */}
          <div className="border-t border-line pt-5 space-y-3">
            <h2 className="text-[11px] font-bold uppercase tracking-wider text-muted">
              {t('productInfo')}
            </h2>
            {product.description && (
              <p className="text-sm text-muted leading-relaxed whitespace-pre-line">
                {product.description}
              </p>
            )}
            <dl className="grid grid-cols-2 gap-x-6 gap-y-2 text-sm">
              <dt className="text-muted">{t('brand')}</dt>
              <dd className="font-medium">{product.brand.name}</dd>

              <dt className="text-muted">{t('gender')}</dt>
              <dd className="font-medium">
                {label(GENDER_LABEL, product.gender, locale) ?? product.gender}
              </dd>

              {product.styleCode && (
                <>
                  <dt className="text-muted">{t('styleCode')}</dt>
                  <dd className="font-mono font-medium">{product.styleCode}</dd>
                </>
              )}

              {product.upperMaterial && (
                <>
                  <dt className="text-muted">{t('material')}</dt>
                  <dd className="font-medium">{product.upperMaterial}</dd>
                </>
              )}

              {product.soleType && (
                <>
                  <dt className="text-muted">{t('sole')}</dt>
                  <dd className="font-medium">{product.soleType}</dd>
                </>
              )}

              {product.closureType && (
                <>
                  <dt className="text-muted">{t('closure')}</dt>
                  <dd className="font-medium">
                    {label(CLOSURE_LABEL, product.closureType, locale) ?? product.closureType}
                  </dd>
                </>
              )}

              {product.shaftStyle && (
                <>
                  <dt className="text-muted">{t('shaft')}</dt>
                  <dd className="font-medium">
                    {label(SHAFT_LABEL, product.shaftStyle, locale) ?? product.shaftStyle}
                  </dd>
                </>
              )}

              <dt className="text-muted">{t('sold')}</dt>
              <dd className="font-medium">{product.soldCount.toLocaleString('vi-VN')}</dd>
            </dl>
          </div>
        </div>
      </div>
    </div>
  );
}
