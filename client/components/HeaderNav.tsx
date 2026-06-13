'use client';

import { useState, useRef } from 'react';
import { Link } from '@/i18n/routing';
import { brandUrl } from '@/lib/cloudinaryUrl';

interface BrandItem {
  id: number;
  name: string;
  slug: string;
  logoPublicId: string | null;
}

interface CategoryItem {
  id: number;
  name: string;
  slug: string;
}

function ChevronDown() {
  return (
    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="6 9 12 15 18 9" />
    </svg>
  );
}

export default function HeaderNav({ brands, categories }: { brands: BrandItem[]; categories: CategoryItem[] }) {
  const [open, setOpen] = useState<'brands' | 'categories' | null>(null);
  const closeTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  function enter(menu: 'brands' | 'categories') {
    if (closeTimer.current) clearTimeout(closeTimer.current);
    setOpen(menu);
  }

  function leave() {
    closeTimer.current = setTimeout(() => setOpen(null), 120);
  }

  return (
    <nav className="hidden md:flex items-center gap-1">
      <Link
        href="/products"
        className="px-3 py-1.5 text-[11px] font-bold uppercase tracking-wider text-muted hover:text-ink transition-colors rounded"
      >
        Sản phẩm
      </Link>

      {/* Brands dropdown */}
      {brands.length > 0 && (
        <div
          className="relative"
          onMouseEnter={() => enter('brands')}
          onMouseLeave={leave}
        >
          <button className="flex items-center gap-1 px-3 py-1.5 text-[11px] font-bold uppercase tracking-wider text-muted hover:text-ink transition-colors rounded">
            Thương hiệu
            <ChevronDown />
          </button>

          {open === 'brands' && (
            <div className="absolute top-full left-0 mt-1 bg-white border border-line shadow-lg rounded-sm min-w-[180px] py-1.5 z-50">
              {brands.map(b => (
                <Link
                  key={b.id}
                  href={`/brands/${b.slug}/products` as never}
                  className="flex items-center gap-2.5 px-4 py-2 text-sm text-muted hover:text-ink hover:bg-paper transition-colors"
                  onClick={() => setOpen(null)}
                >
                  {b.logoPublicId ? (
                    <img
                      src={brandUrl(b.logoPublicId, 48, 24)}
                      alt={b.name}
                      className="h-4 w-8 object-contain flex-shrink-0"
                    />
                  ) : (
                    <span className="w-8 flex-shrink-0" />
                  )}
                  <span className="font-medium">{b.name}</span>
                </Link>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Categories dropdown */}
      {categories.length > 0 && (
        <div
          className="relative"
          onMouseEnter={() => enter('categories')}
          onMouseLeave={leave}
        >
          <button className="flex items-center gap-1 px-3 py-1.5 text-[11px] font-bold uppercase tracking-wider text-muted hover:text-ink transition-colors rounded">
            Danh mục
            <ChevronDown />
          </button>

          {open === 'categories' && (
            <div className="absolute top-full left-0 mt-1 bg-white border border-line shadow-lg rounded-sm min-w-[180px] py-1.5 z-50">
              {categories.map(c => (
                <Link
                  key={c.id}
                  href={`/categories/${c.slug}/products` as never}
                  className="block px-4 py-2 text-sm text-muted hover:text-ink hover:bg-paper transition-colors font-medium"
                  onClick={() => setOpen(null)}
                >
                  {c.name}
                </Link>
              ))}
            </div>
          )}
        </div>
      )}
    </nav>
  );
}
