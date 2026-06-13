'use client';

import { useState } from 'react';
import { Link } from '@/i18n/routing';
import { useTranslations } from 'next-intl';
import { productUrl } from '@/lib/cloudinaryUrl';
import { type ProductResponse, formatPrice } from './types';

export default function ProductCard({ product }: { product: ProductResponse }) {
  const t = useTranslations('products');
  const [colorIdx, setColorIdx] = useState(0);

  const color = product.colors[colorIdx] ?? product.colors[0];
  const price = color?.price ?? product.basePrice;
  const imgSrc = color?.primaryImagePublicId
    ? productUrl(color.primaryImagePublicId, 400, 400)
    : null;

  return (
    <Link href={`/products/${product.slug}`} className="group block">
      <div className="relative aspect-square bg-paper rounded-sm overflow-hidden border border-line">
        {imgSrc ? (
          <img
            src={imgSrc}
            alt={product.name}
            className="w-full h-full object-contain p-3 group-hover:scale-105 transition-transform duration-300"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-muted text-xs">
            —
          </div>
        )}
        <div className="absolute top-2 left-2 flex flex-col gap-1">
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
      </div>

      <div className="mt-2.5 space-y-0.5">
        <p className="text-[11px] text-muted font-medium uppercase tracking-wide">{product.brand.name}</p>
        <p className="font-bold text-sm leading-snug line-clamp-2">{product.name}</p>
        <div className="flex items-baseline gap-2 pt-0.5">
          <span className="font-display font-black text-sm">{formatPrice(price)}</span>
          {product.originalPrice != null && product.originalPrice > price && (
            <span className="text-xs text-muted line-through">{formatPrice(product.originalPrice)}</span>
          )}
        </div>
      </div>

      {product.colors.length > 1 && (
        <div
          className="mt-2 flex gap-1.5 flex-wrap"
          onClick={e => e.preventDefault()}
        >
          {product.colors.map((c, i) => (
            <button
              key={c.colorway}
              title={c.colorway}
              onClick={e => {
                e.preventDefault();
                e.stopPropagation();
                setColorIdx(i);
              }}
              className={`w-3.5 h-3.5 rounded-full border-2 transition-all ${
                i === colorIdx ? 'border-ink scale-110' : 'border-transparent hover:border-muted'
              }`}
              style={{ backgroundColor: c.colorHex ?? '#aaa' }}
            />
          ))}
        </div>
      )}
    </Link>
  );
}
