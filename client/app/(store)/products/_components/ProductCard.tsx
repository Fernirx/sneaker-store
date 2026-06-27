'use client';

import { useState } from 'react';
import Link from 'next/link';
import { productUrl } from '@/lib/cloudinaryUrl';
import { type ProductResponse, formatPrice } from './types';

export default function ProductCard({ product }: { product: ProductResponse }) {
  const [selectedIdx, setSelectedIdx] = useState<number | null>(product.colors.length === 1 ? 0 : null);

  // Mặc định hiển thị ảnh của màu đầu tiên (0) nếu chưa chọn màu nào
  const displayColor = selectedIdx != null ? product.colors[selectedIdx] : product.colors[0];
  const imgSrc = displayColor?.primaryImagePublicId
    ? productUrl(displayColor.primaryImagePublicId, 400, 400)
    : null;

  // Luôn hiển thị khoảng giá Min - Max của sản phẩm (vì chọn màu ngoài thẻ chưa chọn size)
  const priceNode = product.minPrice != null ? (
    <div className="flex items-baseline justify-center gap-2 pt-0.5">
      <span className="text-xs font-normal text-ink">
        {product.maxPrice != null && product.maxPrice !== product.minPrice 
          ? `${formatPrice(product.minPrice)} - ${formatPrice(product.maxPrice)}` 
          : formatPrice(product.minPrice)}
      </span>
    </div>
  ) : (
    <div className="pt-0.5 text-xs text-muted font-normal text-center">Chưa có giá</div>
  );

  return (
    <Link href={`/products/${product.slug}`} className="group block">
      <div className="relative aspect-square bg-transparent rounded-sm overflow-hidden">
        {imgSrc ? (
          <img
            src={imgSrc}
            alt={product.name}
            className="w-full h-full object-contain group-hover:scale-105 transition-transform duration-300"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-muted text-xs">
            —
          </div>
        )}
        <div className="absolute top-2 left-2 flex flex-col gap-1">
          {product.newArrival && (
            <span className="bg-ink text-white text-[9px] font-bold px-2 py-0.5 rounded-sm uppercase">
              {"MỚI"}
            </span>
          )}
          {product.onSale && (
            <span className="bg-accent text-white text-[9px] font-bold px-2 py-0.5 rounded-sm uppercase">
              {"SALE"}
            </span>
          )}
        </div>
      </div>

      <div className="mt-2.5 flex flex-col items-center text-center">
        <p className="text-[11px] text-muted font-normal uppercase tracking-wide">{product.brand.name}</p>
        <p className="font-bold text-sm leading-snug line-clamp-2 text-ink mt-0.5">{product.name}</p>
        <div className="mt-2 w-full flex justify-center">
          {priceNode}
        </div>
      </div>

      {product.colors.length > 0 && (
        <div
          className="mt-2 flex gap-1.5 flex-wrap justify-center"
          onClick={e => e.preventDefault()}
        >
          {product.colors.map((c, i) => {
            const isSelected = selectedIdx != null ? i === selectedIdx : (product.colors.length === 1 && i === 0);
            return (
              <button
                key={c.colorway}
                title={c.colorway}
                onClick={e => {
                  e.preventDefault();
                  e.stopPropagation();
                  setSelectedIdx(i === selectedIdx && product.colors.length > 1 ? null : i);
                }}
                className={`w-3.5 h-3.5 rounded-full border border-black/25 transition-all duration-200 ${
                  isSelected ? 'ring-[1.5px] ring-ink ring-offset-1 scale-125 border-black/40' : 'hover:scale-110 hover:border-black/50'
                }`}
                style={{ backgroundColor: c.colorHex ?? '#aaa' }}
              />
            );
          })}
        </div>
      )}
    </Link>
  );
}

